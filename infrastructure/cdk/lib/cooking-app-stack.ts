import * as cdk from 'aws-cdk-lib';
import * as dynamodb from 'aws-cdk-lib/aws-dynamodb';
import * as s3 from 'aws-cdk-lib/aws-s3';
import * as cognito from 'aws-cdk-lib/aws-cognito';
import * as apigateway from 'aws-cdk-lib/aws-apigateway';
import { Construct } from 'constructs';

interface CookingAppStackProps extends cdk.StackProps {
  stage: string;
}

export class CookingAppStack extends cdk.Stack {
  constructor(scope: Construct, id: string, props: CookingAppStackProps) {
    super(scope, id, props);

    const { stage } = props;

    // DynamoDB Tables
    this.createDynamoDBTables(stage);

    // S3 Bucket
    this.createS3Bucket(stage);

    // Cognito User Pool
    this.createCognitoUserPool(stage);

    // API Gateway
    this.createApiGateway(stage);
  }

  private createDynamoDBTables(stage: string) {
    // Users Table
    const usersTable = new dynamodb.Table(this, 'UsersTable', {
      tableName: `cooking-app-users-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      pointInTimeRecovery: stage === 'prod',
    });

    // Recipes Table
    const recipesTable = new dynamodb.Table(this, 'RecipesTable', {
      tableName: `cooking-app-recipes-${stage}`,
      partitionKey: { name: 'RecipeId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
      pointInTimeRecovery: stage === 'prod',
    });

    // GSI for Recipes by Author
    recipesTable.addGlobalSecondaryIndex({
      indexName: 'GSI_Author',
      partitionKey: { name: 'AuthorId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });

    // GSI for Recipes by Category
    recipesTable.addGlobalSecondaryIndex({
      indexName: 'GSI_Category',
      partitionKey: { name: 'Category', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });

    // Schedules Table
    new dynamodb.Table(this, 'SchedulesTable', {
      tableName: `cooking-app-schedules-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'DateTypeRecipeId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // ShoppingLists Table
    new dynamodb.Table(this, 'ShoppingListsTable', {
      tableName: `cooking-app-shopping-lists-${stage}`,
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'ItemId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // Reviews Table
    const reviewsTable = new dynamodb.Table(this, 'ReviewsTable', {
      tableName: `cooking-app-reviews-${stage}`,
      partitionKey: { name: 'RecipeId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'ReviewId', type: dynamodb.AttributeType.STRING },
      billingMode: dynamodb.BillingMode.PAY_PER_REQUEST,
      removalPolicy: stage === 'prod' ? cdk.RemovalPolicy.RETAIN : cdk.RemovalPolicy.DESTROY,
    });

    // GSI for Reviews by User
    reviewsTable.addGlobalSecondaryIndex({
      indexName: 'GSI_User',
      partitionKey: { name: 'UserId', type: dynamodb.AttributeType.STRING },
      sortKey: { name: 'CreatedAt', type: dynamodb.AttributeType.STRING },
    });
  }

  private createS3Bucket(stage: string) {
    new s3.Bucket(this, 'ImagesBucket', {
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
  }

  private createCognitoUserPool(stage: string) {
    const userPool = new cognito.UserPool(this, 'UserPool', {
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
    userPool.addClient('WebClient', {
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
      value: userPool.userPoolId,
      description: 'Cognito User Pool ID',
    });
  }

  private createApiGateway(stage: string) {
    const api = new apigateway.RestApi(this, 'CookingAppApi', {
      restApiName: `cooking-app-api-${stage}`,
      description: 'Cooking Support App REST API',
      deployOptions: {
        stageName: stage,
        throttlingRateLimit: 1000,
        throttlingBurstLimit: 2000,
      },
      defaultCorsPreflightOptions: {
        allowOrigins: apigateway.Cors.ALL_ORIGINS, // 本番環境では特定のドメインに制限
        allowMethods: apigateway.Cors.ALL_METHODS,
        allowHeaders: ['Content-Type', 'Authorization'],
      },
    });

    // Output
    new cdk.CfnOutput(this, 'ApiUrl', {
      value: api.url,
      description: 'API Gateway URL',
    });
  }
}
