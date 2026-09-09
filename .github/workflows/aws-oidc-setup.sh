#!/bin/bash

# ===========================================================================================
#
# AWS GitHub Actions OIDC Setup Script
#
# Step 1: Verify AWS CLI access
# Step 2: Create GitHub OIDC Identity Provider
# Step 3: Create IAM Role for GitHub Actions
# Step 4: Attach AdministratorAccess policy (replace later with least privilege)
# Step 5: Output values required for GitHub Actions
#
# ===========================================================================================

set -e

# -----------------------------------------------------------------------------
# CONFIGURATION
# -----------------------------------------------------------------------------

# AWS Region
export AWS_REGION="us-east-1"

# GitHub Organization
export GITHUB_ORG="devopswithcloud"

# GitHub Repository
export GITHUB_REPO="i27-dashboard-b10"

# IAM Role Name
export ROLE_NAME="GitHubActionsRole"

# -----------------------------------------------------------------------------

echo "======================================"
echo " AWS GitHub OIDC Setup"
echo "======================================"

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)

echo "AWS Account : $ACCOUNT_ID"
echo "Region      : $AWS_REGION"
echo "GitHub Org  : $GITHUB_ORG"
echo "GitHub Repo : $GITHUB_REPO"
echo "Role Name   : $ROLE_NAME"

echo ""

# -----------------------------------------------------------------------------
# STEP 1 - Verify AWS Access
# -----------------------------------------------------------------------------

echo "Step 1 - Checking AWS credentials..."

aws sts get-caller-identity >/dev/null

echo "AWS credentials verified"
echo ""

# -----------------------------------------------------------------------------
# STEP 2 - Create GitHub OIDC Provider
# -----------------------------------------------------------------------------

echo "Step 2 - Creating GitHub OIDC Provider..."

aws iam create-open-id-connect-provider \
    --url https://token.actions.githubusercontent.com \
    --client-id-list sts.amazonaws.com \
    --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1 \
    >/dev/null 2>&1 || echo "OIDC Provider already exists"

echo "OIDC Provider Ready"
echo ""

# -----------------------------------------------------------------------------
# STEP 3 - Create Trust Policy
# -----------------------------------------------------------------------------

cat > trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect":"Allow",
      "Principal":{
        "Federated":"arn:aws:iam::$ACCOUNT_ID:oidc-provider/token.actions.githubusercontent.com"
      },
      "Action":"sts:AssumeRoleWithWebIdentity",
      "Condition":{
        "StringEquals":{
          "token.actions.githubusercontent.com:aud":"sts.amazonaws.com"
        },
        "StringLike":{
          "token.actions.githubusercontent.com:sub":"repo:${GITHUB_ORG}/${GITHUB_REPO}:*"
        }
      }
    }
  ]
}
EOF

echo "Trust policy created"
echo ""

# -----------------------------------------------------------------------------
# STEP 4 - Create IAM Role
# -----------------------------------------------------------------------------

echo "Creating IAM Role..."

aws iam create-role \
    --role-name "$ROLE_NAME" \
    --assume-role-policy-document file://trust-policy.json \
    >/dev/null 2>&1 || echo "Role already exists"

echo "IAM Role Ready"
echo ""

# -----------------------------------------------------------------------------
# STEP 5 - Attach Policy
# -----------------------------------------------------------------------------

echo "Attaching AdministratorAccess..."

aws iam attach-role-policy \
    --role-name "$ROLE_NAME" \
    --policy-arn arn:aws:iam::aws:policy/AdministratorAccess

echo "Policy attached"
echo ""

# -----------------------------------------------------------------------------
# STEP 6 - OUTPUT
# -----------------------------------------------------------------------------

ROLE_ARN=$(aws iam get-role \
    --role-name "$ROLE_NAME" \
    --query "Role.Arn" \
    --output text)

echo "======================================"
echo "SETUP COMPLETE"
echo "======================================"

echo ""

echo "Add these GitHub Secrets (if needed):"

echo ""
echo "AWS_REGION"
echo "$AWS_REGION"

echo ""
echo "AWS_ROLE_ARN"
echo "$ROLE_ARN"

echo ""

echo "======================================"
echo "GitHub Workflow Values"
echo "======================================"

echo "role-to-assume: $ROLE_ARN"
echo "aws-region:     $AWS_REGION"

echo ""
echo "======================================"
echo "NEXT STEPS"
echo "======================================"

echo "1. Push GitHub workflow"
echo "2. Use aws-actions/configure-aws-credentials"
echo "3. Deploy resources"