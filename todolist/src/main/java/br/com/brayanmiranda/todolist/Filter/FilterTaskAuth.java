package br.com.brayanmiranda.todolist.Filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.brayanmiranda.todolist.Users.IUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter{

    @Autowired
    private IUserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
      
            var serveltPath = request.getServletPath();
            if(serveltPath.startsWith("/tasks/")){
                //Pegar a autenticação (usuario e senha )
                    var authorization = request.getHeader("Authorization");
                    //Eu quero que vc pegue a palavra basic tire e tire os espaços .trim()
                    var authEncoded = authorization.substring("Basic".length()).trim();
                    //Converta em bytes
                    byte[] authDecode = Base64.getDecoder().decode(authEncoded);
                    //Tranforme os bytes em uma sting q possa ser lida
                    var authSting = new String(authDecode);
                    //Trasnforma em um array q possa ser lido separando-os por ':'
                    String[] credentials = authSting.split(":");
                    //Separe as duas informaçoões 
                    String username = credentials[0];
                    String password = credentials[1];

                //Validar usuario
                var user = this.userRepository.findByUsername(username);
                if(user == null){
                    response.sendError(401);
                }else{
                    //Validar senha 
                    //Comparação de 2 arrays de char por isso a conversão
                    var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(),user.getPassword());
                    //Converte para uma variavel booleana 
                    if (passwordVerify.verified){

                        request.setAttribute("idUser", user.getId());
                        //Segue viagem
                        filterChain.doFilter(request,response);
                    }else{
                        response.sendError(401);
                    }
                }
            }else{
                filterChain.doFilter(request,response);
            }
    }


        
    
    
}
