package com.cookingapp.infrastructure.external.cognito;

import com.cookingapp.domain.exception.AuthenticationException;
import com.cookingapp.domain.exception.UserAlreadyExistsException;
import com.cookingapp.infrastructure.external.cognito.dto.AuthTokens;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * AWS Cognito認証サービス
 * ユーザー登録、ログイン、トークン管理を提供
 */
@Service
public class CognitoAuthService {
    
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final String clientId;
    private final String clientSecret;
    private final boolean useClientSecret;

    public CognitoAuthService(
            CognitoIdentityProviderClient cognitoClient,
            @Value("${aws.cognito.userPoolId}") String userPoolId,
            @Value("${aws.cognito.clientId}") String clientId,
            @Value("${aws.cognito.clientSecret:}") String clientSecret) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.useClientSecret = clientSecret != null && !clientSecret.isEmpty();
    }

    /**
     * ユーザー登録
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param nickname ニックネーム
     * @return ユーザーID（Cognito Sub）
     * @throws UserAlreadyExistsException ユーザーが既に存在する場合
     * @throws AuthenticationException 登録に失敗した場合
     */
    public String signUp(String email, String password, String nickname) {
        try {
            AttributeType emailAttr = AttributeType.builder()
                    .name("email")
                    .value(email)
                    .build();
            
            AttributeType nameAttr = AttributeType.builder()
                    .name("name")
                    .value(nickname)
                    .build();
            
            AttributeType nicknameAttr = AttributeType.builder()
                    .name("nickname")
                    .value(nickname)
                    .build();

            SignUpRequest.Builder signUpRequestBuilder = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .password(password)
                    .userAttributes(emailAttr, nameAttr, nicknameAttr);
            
            // ClientSecretがある場合のみSECRET_HASHを追加
            if (useClientSecret) {
                signUpRequestBuilder.secretHash(calculateSecretHash(email));
            }
            
            SignUpRequest signUpRequest = signUpRequestBuilder.build();

            SignUpResponse response = cognitoClient.signUp(signUpRequest);
            
            return response.userSub();
            
        } catch (UsernameExistsException e) {
            throw new UserAlreadyExistsException("User already exists: " + email, e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to sign up user: " + e.getMessage(), e);
        }
    }

    /**
     * メール確認コードの検証
     * 
     * @param email メールアドレス
     * @param confirmationCode 確認コード
     * @throws AuthenticationException 確認に失敗した場合
     */
    public void confirmSignUp(String email, String confirmationCode) {
        try {
            ConfirmSignUpRequest.Builder confirmRequestBuilder = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(email)
                    .confirmationCode(confirmationCode);
            
            // ClientSecretがある場合のみSECRET_HASHを追加
            if (useClientSecret) {
                confirmRequestBuilder.secretHash(calculateSecretHash(email));
            }
            
            ConfirmSignUpRequest confirmRequest = confirmRequestBuilder.build();
            cognitoClient.confirmSignUp(confirmRequest);
            
        } catch (CodeMismatchException e) {
            throw new AuthenticationException("Invalid confirmation code", e);
        } catch (ExpiredCodeException e) {
            throw new AuthenticationException("Confirmation code has expired", e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to confirm sign up: " + e.getMessage(), e);
        }
    }

    /**
     * 確認コードの再送信
     * 
     * @param email メールアドレス
     * @throws AuthenticationException 再送信に失敗した場合
     */
    public void resendConfirmationCode(String email) {
        try {
            ResendConfirmationCodeRequest.Builder resendRequestBuilder = ResendConfirmationCodeRequest.builder()
                    .clientId(clientId)
                    .username(email);
            
            // ClientSecretがある場合のみSECRET_HASHを追加
            if (useClientSecret) {
                resendRequestBuilder.secretHash(calculateSecretHash(email));
            }
            
            ResendConfirmationCodeRequest resendRequest = resendRequestBuilder.build();
            cognitoClient.resendConfirmationCode(resendRequest);
            
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to resend confirmation code: " + e.getMessage(), e);
        }
    }

    /**
     * ログイン
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @return 認証トークン
     * @throws AuthenticationException 認証に失敗した場合
     */
    public AuthTokens signIn(String email, String password) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", password);
            
            // ClientSecretがある場合のみSECRET_HASHを追加
            if (useClientSecret) {
                authParams.put("SECRET_HASH", calculateSecretHash(email));
            }

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType authResult = authResponse.authenticationResult();

            return new AuthTokens(
                    authResult.accessToken(),
                    authResult.refreshToken(),
                    authResult.idToken(),
                    authResult.expiresIn()
            );
            
        } catch (NotAuthorizedException | UserNotFoundException e) {
            throw new AuthenticationException("Invalid email or password", e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to sign in: " + e.getMessage(), e);
        }
    }

    /**
     * トークンのリフレッシュ
     * 
     * @param refreshToken リフレッシュトークン
     * @return 新しい認証トークン
     * @throws AuthenticationException リフレッシュに失敗した場合
     */
    public AuthTokens refreshToken(String refreshToken, String username) {
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("REFRESH_TOKEN", refreshToken);
            
            // ClientSecretがある場合のみSECRET_HASHを追加
            if (useClientSecret) {
                authParams.put("SECRET_HASH", calculateSecretHash(username));
            }

            InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                    .clientId(clientId)
                    .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                    .authParameters(authParams)
                    .build();

            InitiateAuthResponse authResponse = cognitoClient.initiateAuth(authRequest);
            AuthenticationResultType authResult = authResponse.authenticationResult();

            return new AuthTokens(
                    authResult.accessToken(),
                    refreshToken, // リフレッシュトークンは変わらない
                    authResult.idToken(),
                    authResult.expiresIn()
            );
            
        } catch (NotAuthorizedException e) {
            throw new AuthenticationException("Invalid or expired refresh token", e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * トークンの検証
     * 
     * @param accessToken アクセストークン
     * @return ユーザーID（Cognito Sub）
     * @throws AuthenticationException トークンが無効な場合
     */
    public String validateToken(String accessToken) {
        try {
            GetUserRequest getUserRequest = GetUserRequest.builder()
                    .accessToken(accessToken)
                    .build();

            GetUserResponse response = cognitoClient.getUser(getUserRequest);
            return response.username();
            
        } catch (NotAuthorizedException e) {
            throw new AuthenticationException("Invalid or expired access token", e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to validate token: " + e.getMessage(), e);
        }
    }

    /**
     * ユーザー削除
     * 
     * @param username ユーザー名（メールアドレス）
     * @throws AuthenticationException 削除に失敗した場合
     */
    public void deleteUser(String username) {
        try {
            AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            cognitoClient.adminDeleteUser(deleteRequest);
            
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to delete user: " + e.getMessage(), e);
        }
    }

    /**
     * アクセストークンからユーザー情報を取得
     * 
     * @param accessToken アクセストークン
     * @return ユーザー属性のマップ
     * @throws AuthenticationException トークンが無効な場合
     */
    public Map<String, String> getUserAttributes(String accessToken) {
        try {
            GetUserRequest getUserRequest = GetUserRequest.builder()
                    .accessToken(accessToken)
                    .build();

            GetUserResponse response = cognitoClient.getUser(getUserRequest);
            
            Map<String, String> attributes = new HashMap<>();
            attributes.put("username", response.username());
            
            for (AttributeType attr : response.userAttributes()) {
                attributes.put(attr.name(), attr.value());
            }
            
            return attributes;
            
        } catch (NotAuthorizedException e) {
            throw new AuthenticationException("Invalid or expired access token", e);
        } catch (CognitoIdentityProviderException e) {
            throw new AuthenticationException("Failed to get user attributes: " + e.getMessage(), e);
        }
    }

    /**
     * SECRET_HASHの計算
     * Cognitoクライアントシークレットを使用する場合に必要
     */
    private String calculateSecretHash(String username) {
        if (!useClientSecret) {
            throw new IllegalStateException("Client secret is not configured");
        }
        try {
            String message = username + clientId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    clientSecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            );
            mac.init(secretKey);
            byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate secret hash", e);
        }
    }
}
