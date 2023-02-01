package dev.andrewohara.getit.spring

import dev.andrewohara.getit.GetItService
import dev.andrewohara.getit.api.Authorizer
import dev.andrewohara.getit.dao.DynamoItemsDao
import dev.andrewohara.getit.dao.DynamoListsDao
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter

@SpringBootApplication(scanBasePackages = ["dev.andrewohara.getit.spring"])
@EnableWebSecurity
class GetItSpringApp {

    @Bean
    fun service(listsDao: DynamoListsDao, itemsDao: DynamoItemsDao) = GetItService(
        items = itemsDao,
        lists = listsDao
    )

    @Bean
    fun security(http: HttpSecurity, authorizer: Authorizer): SecurityFilterChain = http
        .authorizeRequests {
            it.anyRequest().authenticated()
        }.exceptionHandling {
            it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        }
        .csrf {
            it.disable()
        }
        .addFilterBefore(JwtSecurityFilter(authorizer), BasicAuthenticationFilter::class.java)
        .build()
}

fun main(args: Array<String>) {
    SpringApplicationBuilder(GetItSpringApp::class.java)
        .profiles("main")
        .run(*args)
}
