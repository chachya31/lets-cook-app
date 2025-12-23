import * as cdk from 'aws-cdk-lib';
import * as amplify from 'aws-cdk-lib/aws-amplify';
import * as apigateway from 'aws-cdk-lib/aws-apigateway';
import * as cognito from 'aws-cdk-lib/aws-cognito';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as iam from 'aws-cdk-lib/aws-iam';
import * as lambda from 'aws-cdk-lib/aws-lambda';
import * as s3 from 'aws-cdk-lib/aws-s3';
import { Construct } from 'constructs';

interface CookingAppStackProps extends cdk.StackProps {
  stage: string;
}

export class CookingAppStack extends cdk.Stack {
  private userPool!: cognito.UserPool;
  private userPoolClient!: cognito.UserPoolClient;
  private imagesBucket!: s3.Bucket;
  private tables: { [key: string]: dynamodb.Table } = {};

  constructor(scope: Construct, id: string, props: CookingAppStackProps) {
    super(scope, id, props);

    const { stage } = props;

    // DynamoDB Tables
    this.createDynamoDBTables(stage);

    // S3 Bucket
    this.createS3Bucket(stage);

    // Cognito User Pool
    this.createCognitoUserPool(stage);

    // Lambda Function
    const lambdaFunction = this.createLambdaFunction(stage);

    // API Gateway
    this.createApiGateway(stage, lambdaFunction);

    // Amplify Hosting (optional - GitHubトークンが必要)
    // this.createAmplifyHosting(stage);
  }

  private createDynamoDBTables(stage: string) {
    // Users Table
    this.tables.users = new dynamodb.Table(this, 'UsersTable', {
      tableName: `cooking-app-users-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      pointInTimeRecovery: stage === 'prod',
    });

    // Recipes Table
    // 設計書通り、RecipeIdのみをパーティションキーとする（ソートキーなし）
    // RecipeIdはUUIDで一意なため、ソートキーは不要
    this.tables.recipes = new dynamodb.Table(this, 'RecipesTable', {
      tableName: `cooking-app-recipes-${stage}`,
      partitionKey: { name: 'RecipeId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      pointInTimeRecovery: stage === 'prod',
    });

    // GSI for Recipes by Author
    this.tables.recipes.addGlobalSecondaryIndex({
      indexName: 'GSI_Author',
      partitionKey: { name: 'AuthorId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });

    // GSI for Recipes by Category
    this.tables.recipes.addGlobalSecondaryIndex({
      indexName: 'GSI_Category',
      partitionKey: { name: 'Category', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });

    // Schedules Table
    this.tables.schedules = new dynamodb.Table(this, 'SchedulesTable', {
      tableName: `cooking-app-schedules-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: {
        name: 'DateRecipeId',
        type: dynamodb.AttributeType.STRING,
      },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // ShoppingLists Table
    this.tables.shoppingLists = new dynamodb.Table(this, 'ShoppingListsTable', {
      tableName: `cooking-app-shopping-lists-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'ItemId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // GSI for NormalizedKey lookup (same name + unit)
    this.tables.shoppingLists.addGlobalSecondaryIndex({
      indexName: 'GSI_NormalizedKey',
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'NormalizedKey', type: dynamodb.AttributeType.STRING },
      projectionType: dynamodb.ProjectionType.ALL,
    });

    // Reviews Table
    this.tables.reviews = new dynamodb.Table(this, 'ReviewsTable', {
      tableName: `cooking-app-reviews-${stage}`,
      partitionKey: { name: 'RecipeId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'ReviewId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // GSI for Reviews by User
    this.tables.reviews.addGlobalSecondaryIndex({
      indexName: 'GSI_User',
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });
  }

  private createS3Bucket(stage: string) {
    this.imagesBucket = new s3.Bucket(this, 'ImagesBucket', {
      bucketName: `cooking-app-images-${stage}`,
      versioned: true,
      encryption: s3.BucketEncryption.S3_MANAGED,
      blockPublicAccess: s3.BlockPublicAccess.BLOCK_ALL,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      autoDeleteObjects: stage !== 'prod',
      lifecycleRules: [
        {
          noncurrentVersionExpiration: cdk.Duration.days(90),
        },
      ],
      cors: [
        {
          allowedMethods: [s3.HttpMethods.GET, s3.HttpMethods.PUT, s3.HttpMethods.POST],
          allowedOrigins: ['*'], // 本番環境では特定のドメインに制限
          allowedHeaders: ['*'],
        },
      ],
    });

    new cdk.CfnOutput(this, 'ImagesBucketName', {
      value: this.imagesBucket.bucketName,
      description: 'S3 Bucket for Images',
    });
  }

  private createCognitoUserPool(stage: string) {
    this.userPool = new cognito.UserPool(this, 'UserPool', {
      userPoolName: `cooking-app-users-${stage}`,
      selfSignUpEnabled: true,
      signInAliases: {
        email: true,
      },
      autoVerify: {
        email: true,
      },
      passwordPolicy: {
        minLength: 8,
        requireLowercase: true,
        requireUppercase: true,
        requireDigits: true,
        requireSymbols: false,
      },
      accountRecovery: cognito.AccountRecovery.EMAIL_ONLY,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // User Pool Client
    this.userPoolClient = this.userPool.addClient('WebClient', {
      userPoolClientName: `cooking-app-web-client-${stage}`,
      authFlows: {
        userPassword: true,
        userSrp: true,
      },
      accessTokenValidity: cdk.Duration.hours(1),
      refreshTokenValidity: cdk.Duration.days(90),
    });

    // Output
    new cdk.CfnOutput(this, 'UserPoolId', {
      value: this.userPool.userPoolId,
      description: 'Cognito User Pool ID',
    });

    new cdk.CfnOutput(this, 'UserPoolClientId', {
      value: this.userPoolClient.userPoolClientId,
      description: 'Cognito User Pool Client ID',
    });
  }

  private createLambdaFunction(stage: string): lambda.Function {
    // Lambda実行ロール
    const lambdaRole = new iam.Role(this, 'LambdaExecutionRole', {
      assumedBy: new iam.ServicePrincipal('lambda.amazonaws.com'),
      managedPolicies: [
        iam.ManagedPolicy.fromAwsManagedPolicyName('service-role/AWSLambdaBasicExecutionRole'),
      ],
    });

    // DynamoDBへのアクセス権限
    Object.values(this.tables).forEach((table) => {
      table.grantReadWriteData(lambdaRole);
    });

    // DynamoDB Query権限を明示的に追加（GSI含む）
    lambdaRole.addToPolicy(
      new iam.PolicyStatement({
        actions: ['dynamodb:Query', 'dynamodb:Scan'],
        resources: [
          ...Object.values(this.tables).map((table) => table.tableArn),
          ...Object.values(this.tables).map((table) => `${table.tableArn}/index/*`),
        ],
      })
    );

    // S3へのアクセス権限
    this.imagesBucket.grantReadWrite(lambdaRole);

    // Cognitoへのアクセス権限
    lambdaRole.addToPolicy(
      new iam.PolicyStatement({
        actions: [
          'cognito-idp:AdminGetUser',
          'cognito-idp:AdminCreateUser',
          'cognito-idp:AdminDeleteUser',
          'cognito-idp:AdminListGroupsForUser',
        ],
        resources: [this.userPool.userPoolArn],
      })
    );

    // Lambda関数
    // 注意: デプロイ前に backend/build/libs/backend-1.0.0.jar が存在することを確認してください
    const lambdaFunction = new lambda.Function(this, 'CookingAppFunction', {
      functionName: `cooking-app-${stage}`,
      runtime: lambda.Runtime.JAVA_21,
      handler: 'com.cookingapp.StreamLambdaHandler::handleRequest',
      //code: lambda.Code.fromAsset('../backend/build/libs/backend-1.0.0.jar'),
      code: lambda.Code.fromAsset('../../backend/build/libs/cooking-support-app-1.0.0.jar'),
      memorySize: 1024,
      timeout: cdk.Duration.seconds(30),
      role: lambdaRole,
      environment: {
        SPRING_PROFILES_ACTIVE: stage,
        SPRING_MAIN_WEB_APPLICATION_TYPE: 'none',
        AWS_REGION_NAME: this.region,
        DYNAMODB_USERS_TABLE: this.tables.users.tableName,
        DYNAMODB_RECIPES_TABLE: this.tables.recipes.tableName,
        DYNAMODB_SCHEDULES_TABLE: this.tables.schedules.tableName,
        DYNAMODB_SHOPPING_LISTS_TABLE: this.tables.shoppingLists.tableName,
        DYNAMODB_REVIEWS_TABLE: this.tables.reviews.tableName,
        S3_BUCKET_NAME: this.imagesBucket.bucketName,
        AWS_COGNITO_USER_POOL_ID: this.userPool.userPoolId,
        AWS_COGNITO_CLIENT_ID: this.userPoolClient.userPoolClientId,
      },
    });

    new cdk.CfnOutput(this, 'LambdaFunctionArn', {
      value: lambdaFunction.functionArn,
      description: 'Lambda Function ARN',
    });

    return lambdaFunction;
  }

  private createApiGateway(stage: string, lambdaFunction: lambda.Function) {
    const api = new apigateway.RestApi(this, 'CookingAppApi', {
      restApiName: `cooking-app-api-${stage}`,
      description: 'Cooking Support App REST API',
      deployOptions: {
        stageName: stage,
        throttlingRateLimit: 1000,
        throttlingBurstLimit: 2000,
      },
      defaultCorsPreflightOptions: {
        allowOrigins: apigateway.Cors.ALL_ORIGINS,
        allowMethods: apigateway.Cors.ALL_METHODS,
        allowHeaders: [
          'Content-Type',
          'Authorization',
          'Accept',
          'Accept-Language',
          'X-User-Id',
          'X-Requested-With',
        ],
      },
      // Lambda + API Gatewayでmultipart/form-data をバイナリとして処理するように設定を追加
      binaryMediaTypes: ['multipart/form-data'],
    });

    // Lambda統合
    const lambdaIntegration = new apigateway.LambdaIntegration(lambdaFunction, {
      proxy: true,
    });

    // すべてのリクエストをLambdaにプロキシ
    api.root.addProxy({
      defaultIntegration: lambdaIntegration,
      anyMethod: true,
    });

    // Output
    new cdk.CfnOutput(this, 'ApiUrl', {
      value: api.url,
      description: 'API Gateway URL',
    });
  }

  private createAmplifyHosting(stage: string) {
    // GitHub Personal Access Tokenが必要
    // Secrets Managerまたは環境変数から取得
    const githubToken = cdk.SecretValue.secretsManager('github-token');

    const amplifyApp = new amplify.CfnApp(this, 'AmplifyApp', {
      name: `cooking-app-${stage}`,
      // repository: 'https://github.com/YOUR_USERNAME/YOUR_REPO', // 要変更
      repository: 'https://github.com/chachya31/lets-cook-app',
      accessToken: githubToken.unsafeUnwrap(),
      buildSpec: `
version: 1
frontend:
  phases:
    preBuild:
      commands:
        - cd frontend
        - npm ci
    build:
      commands:
        - npm run build
  artifacts:
    baseDirectory: frontend/dist
    files:
      - '**/*'
  cache:
    paths:
      - frontend/node_modules/**/*
      `,
      environmentVariables: [
        {
          name: 'VITE_API_URL',
          value: '${ApiUrl}', // API Gateway URLを参照
        },
      ],
    });

    new amplify.CfnBranch(this, 'AmplifyBranch', {
      appId: amplifyApp.attrAppId,
      branchName: stage === 'prod' ? 'main' : 'develop',
      enableAutoBuild: true,
    });

    new cdk.CfnOutput(this, 'AmplifyAppUrl', {
      value: `https://${stage}.${amplifyApp.attrDefaultDomain}`,
      description: 'Amplify Hosting URL',
    });
  }
}
