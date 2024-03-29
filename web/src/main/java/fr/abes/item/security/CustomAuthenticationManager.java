package fr.abes.item.security;

import fr.abes.item.constant.Constant;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.service.service.ServiceProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
public class CustomAuthenticationManager implements AuthenticationManager {

    @Autowired
    AuthenticationEventPublisher authenticationEventPublisher;

    @Resource
    private ServiceProvider service;

    private JdbcTemplate jdbcTemplateBaseKopya;

    @Value("${wsAuthSudoc.url}")
    String urlWsAuthSudoc;

    public CustomAuthenticationManager(@Qualifier("itemDataSource") DataSource dataSourceKopya) {

        this.jdbcTemplateBaseKopya = new JdbcTemplate(dataSourceKopya);
    }
    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        log.debug(Constant.ENTER_AUTHENTICATE);

        String name = authentication.getName();
        String password = authentication.getCredentials().toString();

        User u = this.estAuthentifie(name, password);

        if (u != null) {

            u.setMail(this.getEmail(Integer.parseInt(u.getUserNum())));
            List<GrantedAuthority> authorities = new ArrayList<>();
            authorities.add(new SimpleGrantedAuthority(u.getRole()));

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(u, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
            authenticationEventPublisher.publishAuthenticationSuccess(auth);
            return auth;
        }
        else {
            authenticationEventPublisher.publishAuthenticationFailure(new BadCredentialsException(Constant.WRONG_LOGIN_AND_OR_PASS), authentication);
            throw new BadCredentialsException(Constant.WRONG_LOGIN_AND_OR_PASS);
        }
    }


    private User estAuthentifie(String userKey, String password) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            String requestJson = "{\n" +
                    "\t\"userKey\": \"" + userKey + "\",\n" +
                    "\t\"password\": \"" + password + "\"\n" +
                    "}";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            restTemplate.getMessageConverters()
                    .add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
            HttpEntity<String> entity = new HttpEntity<>(requestJson, headers);
            User u = restTemplate.postForObject(this.urlWsAuthSudoc, entity, User.class);
            return u;
        }
        catch (HttpClientErrorException e) {
            log.info(Constant.ERROR_SUDOC_WS_AUTHENTICATION + e);
            return null;
        }
    }
    public String getEmail(Integer userNum) {
        try {
            return service.getUtilisateur().findEmailById(userNum);
        }
        catch (EmptyResultDataAccessException e)
        {
            return ""; // genere l'erreur "the given id must not be null"
        }
    }
}
