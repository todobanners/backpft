package codigocreativo.uy.servidorapp.ws;

import codigocreativo.uy.servidorapp.DTO.UsuarioDto;
import codigocreativo.uy.servidorapp.JWT.JwtService;
import codigocreativo.uy.servidorapp.servicios.UsuarioRemote;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioResource {
    @EJB
    private UsuarioRemote er;
    @EJB
    private JwtService jwtService;

    @POST
    @Path("/crear")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response crearUsuario(UsuarioDto usuario) {
        this.er.crearUsuario(usuario);
        return Response.status(201).build();
    }

    @PUT
    @Path("/modificar")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response modificarUsuario(UsuarioDto usuario){
        try {
            this.er.modificarUsuario(usuario);
            return Response.status(200).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }


    @GET
    @Path("/ListarTodosLosUsuarios")
    public List<UsuarioDto> obtenerTodosLosUsuarios(){
        return this.er.obtenerUsuarios();
    }

    @GET
    @Path("/obtenerUserEmail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEmail(@QueryParam("email") String email) {
        UsuarioDto user = er.findUserByEmail(email);
        if (user != null) {
            return Response.ok(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }


    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest loginRequest) {
        if(loginRequest == null){
            return Response.status(Response.Status.BAD_REQUEST).entity("Login request is null").build();
        }
        UsuarioDto user = this.er.login(loginRequest.getUsuario(),loginRequest.getPassword());
        if (user != null) {
            String token = jwtService.generateToken(user.getEmail());
            System.out.println("Usuario logueado: " + user); // Depuración
            LoginResponse loginResponse = new LoginResponse(token, user);
            System.out.println("Esto es el objeto q se envia al front");
            System.out.println(loginResponse);
            return Response.ok(loginResponse).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
        }
    }

    @POST
    @Path("/google-login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response googleLogin(GoogleLoginRequest googleLoginRequest) {
        if (googleLoginRequest == null) {
            System.out.println("LA solicitud dio null");
            return Response.status(Response.Status.BAD_REQUEST).entity("Login request is null").build();
        }
        System.out.println("Probando");
        System.out.println(googleLoginRequest.getEmail());

        UsuarioDto user = this.er.findUserByEmail(googleLoginRequest.getEmail());
        if (user == null) {
            // Crear nuevo usuario si no existe
            user = new UsuarioDto();
            user.setEmail(googleLoginRequest.getEmail());
            user.setNombre(googleLoginRequest.getName());
            // Agregar otros campos personalizados
            this.er.crearUsuario(user);
        }

        String token = jwtService.generateToken(user.getEmail());
        LoginResponse loginResponse = new LoginResponse(token, user);
        return Response.ok(loginResponse).build();
    }

    // Clases de solicitud y respuesta
    public static class LoginRequest {
        private String usuario;
        private String password;

        public String getUsuario() {
            return usuario;
        }

        public void setUsuario(String usuario) {
            this.usuario = usuario;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class LoginResponse {
        private String token;
        private UsuarioDto user;

        public LoginResponse(String token, UsuarioDto user) {
            this.token = token;
            this.user = user;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
        public UsuarioDto getUser() {
        return user;
        }
        public void setUser(UsuarioDto user) {
        this.user = user;
        }
    }

    // Clases de solicitud y respuesta
    public static class GoogleLoginRequest {
        private String email;
        private String name;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

}
