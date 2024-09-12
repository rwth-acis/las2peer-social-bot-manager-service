package services.socialBotManagerService.chat.github;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.GHAppInstallation;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

// import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;

/**
 * A GitHub app can be installed multiple times (e.g., within different organizations or repositories).
 * To use the GitHub API for an app installation, we need an access token for this app installation.
 * For requesting this access token, a JWT is needed. This JWT allows to authenticate as a GitHub app.
 * The JWT needs to be signed using the app's private key (from general app settings).
 *
 * See https://docs.github.com/en/developers/apps/building-github-apps/authenticating-with-github-apps
 */
public class GitHubAppHelper {

    /**
     * Id of the GitHub app.
     */
    private int gitHubAppId;

    /**
     * Private key used to sign JWTs.
     */
    private Key privateKey;

    /**
     *
     * @param gitHubAppId Id of the GitHub app
     * @param pkcs8PrivateKey Private key of GitHub app (already needs to be converted to pkcs8)
     * @throws GitHubAppHelperException
     */
    public GitHubAppHelper(int gitHubAppId, String pkcs8PrivateKey) throws GitHubAppHelperException {
        this.gitHubAppId = gitHubAppId;

        byte[] pkcs8PrivateKeyBytes = jakarta.xml.bind.DatatypeConverter.parseBase64Binary(pkcs8PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8PrivateKeyBytes);
        try {
            this.privateKey = KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new GitHubAppHelperException(e.getMessage());
        }
    }

    /**
     * Returns a GitHub object that has access to the given repository.
     * @param repositoryFullName Full name of the repository, containing both owner and repository name.
     * @return GitHub object that has access to the given repository.
     */
    public GitHub getGitHubInstance(String repositoryFullName) {
        String ownerName = repositoryFullName.split("/")[0];
        String repoName = repositoryFullName.split("/")[1];

        try {
            // first create GitHub object using a JWT (this is needed to request an access token for an app installation)
            GitHub gitHub = new GitHubBuilder().withJwtToken(generateJWT()).build();

            // get app installation for given repository (getInstallationByRepository requires a JWT)
            GHAppInstallation installation = gitHub.getApp().getInstallationByRepository(ownerName, repoName);

            // create a GitHub object with app installation token
            return new GitHubBuilder().withAppInstallationToken(installation.createToken().create().getToken()).build();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Generates a JWT and signs it with the app's private key.
     * @return JWT
     */
    private String generateJWT() {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + 60000);
        return Jwts.builder()
                .setIssuedAt(now) // issue now
                .setExpiration(expiration) // expiration time of JWT
                .setIssuer(String.valueOf(gitHubAppId)) // app id needs to be used as issuer
                .signWith(this.privateKey, SignatureAlgorithm.RS256) // sign with app's private key
                .compact();
    }

    /**
     * General exception that is thrown if something related to the GitHubAppHelper is not working.
     */
    public class GitHubAppHelperException extends Exception {
        public GitHubAppHelperException(String message) {
            super(message);
        }
    }

}
