package com.algaworks.algafood.core.security.authorizationserver;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.TokenApprovalStore;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter{
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired 
	private UserDetailsService userDetailsService;
	
	@Autowired
	private JwtKeyStoreProperties jwtKeyStoreProperties;
	
	@Autowired
	private DataSource dataSource;
	
	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(dataSource);
			
		/*
		.inMemory()
			//quando o sitema é feito para um cliente só, pode deixar em memory e usuário e senha coloca no application-properties
				.withClient("algafood-web")
				.secret(passwordEncoder.encode("web123"))
				.authorizedGrantTypes("password", "refresh_token")
				.scopes("WRITE", "READ")
				.accessTokenValiditySeconds(60 * 60 * 6)
				.refreshTokenValiditySeconds(60 * 24 * 60 * 60)
			.and()
				.withClient("foodanalytics")
				.secret(passwordEncoder.encode("food123"))
				.authorizedGrantTypes("authorization_code")
				.scopes("WRITE", "READ")
				.redirectUris("http://www.foodanalytics.local:8082")
			.and()
				.withClient("faturamento")
				.secret(passwordEncoder.encode("faturamento123"))
				.authorizedGrantTypes("client_credentials")
				.scopes("READ")
			.and()
				.withClient("webadmin")
				.authorizedGrantTypes("implicit")
				.scopes("WRITE", "READ")
				.redirectUris("http://aplicacao-cliente")
			.and()
				.withClient("checktoken")
				.secret(passwordEncoder.encode("check123"));
		*/
	}
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
		//security.checkTokenAccess("isAuthenticated()");
		security.checkTokenAccess("permitAll()")
			.tokenKeyAccess("permitAll()")
			.allowFormAuthenticationForClients();
	}
	
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		
		var enhacerChain = new TokenEnhancerChain();
		enhacerChain.setTokenEnhancers(Arrays.asList(new JwtCustomClaimsTokenEnhacer(), jwtAccessTokenConverter()));
		
		endpoints.authenticationManager(authenticationManager)
		.userDetailsService(userDetailsService)
		.reuseRefreshTokens(false)
		.accessTokenConverter(jwtAccessTokenConverter())
		.tokenEnhancer(enhacerChain)
		.approvalStore(approvalStore(endpoints.getTokenStore()))
		.tokenGranter(tokenGranter(endpoints));
	}
	
	private ApprovalStore approvalStore(TokenStore tokenStore) {
		var approvalStore = new TokenApprovalStore(); 
		approvalStore.setTokenStore(tokenStore);
		
		return approvalStore;
	}
	
	@Bean
	public JWKSet JWKSet() {
		RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair().getPublic())
				.keyUse(KeyUse.SIGNATURE)
				.algorithm(JWSAlgorithm.RS256)
				.keyID("algafood-key-id");
		return new JWKSet(builder.build());
		
	}
	
	//Trabalhando com JWT
	@Bean
	public JwtAccessTokenConverter jwtAccessTokenConverter() {
		JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
//		jwtAccessTokenConverter.setSigningKey("dgasdgsahgdhbvhjvauyigv41as4846x5c8484dsc844");

		jwtAccessTokenConverter.setKeyPair(keyPair());
		
		return jwtAccessTokenConverter;
	}
	
	private KeyPair keyPair() {
	    var keyStorePass = jwtKeyStoreProperties.getPassword();
	    var keyPairAlias = jwtKeyStoreProperties.getKeypairAlias();
	    
	    var keyStoreKeyFactory = new KeyStoreKeyFactory(jwtKeyStoreProperties.getJksLocation(), keyStorePass.toCharArray());
	    return keyStoreKeyFactory.getKeyPair(keyPairAlias);
	}
	
	private TokenGranter tokenGranter(AuthorizationServerEndpointsConfigurer endpoints) {
		var pkceAuthorizationCodeTokenGranter = new PkceAuthorizationCodeTokenGranter(endpoints.getTokenServices(),
				endpoints.getAuthorizationCodeServices(), endpoints.getClientDetailsService(),
				endpoints.getOAuth2RequestFactory());
		
		var granters = Arrays.asList(
				pkceAuthorizationCodeTokenGranter, endpoints.getTokenGranter());
		
		return new CompositeTokenGranter(granters);
	}
	
}
