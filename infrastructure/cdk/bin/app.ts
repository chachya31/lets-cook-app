#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import { CookingAppStack } from '../lib/cooking-app-stack';

const app = new cdk.App();

// 開発環境
new CookingAppStack(app, 'CookingAppStack-Dev', {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || 'ap-northeast-1',
  },
  stage: 'dev',
});

// 本番環境
new CookingAppStack(app, 'CookingAppStack-Prod', {
  env: {
    account: process.env.CDK_DEFAULT_ACCOUNT,
    region: process.env.CDK_DEFAULT_REGION || 'ap-northeast-1',
  },
  stage: 'prod',
});
