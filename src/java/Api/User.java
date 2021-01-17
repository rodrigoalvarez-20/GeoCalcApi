package Api;

import Utils.XmlHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class User extends HttpServlet {

    private XmlHelper helper = null; //Variable para el Helper de XML

    /*
        Esta funcion permite eliminar los errores de CORS que se generan en los navegadores
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
    }

    //Logea un usuario
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        helper = new XmlHelper(request.getRealPath("/")); //Creo una nueva instancia del helper con el path base
        response.setContentType("application/xml"); //Header del tipo de contenido de respuesta (XML)
        response.addHeader("Access-Control-Allow-Origin", "*"); //Header para permitir las conexiones de cualquier IP
        response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept"); //Header para los Headers aceptados
        String email = request.getParameter("email"); //Obtengo el valor del email
        String pwd = request.getParameter("password"); //Obtengo el valor del password
        HashMap<String, Object> loginRes = helper.loginUser(email, pwd); //Ocupando el helper, obtengo el HashMap de respuesta
        if (loginRes.get("error") != null) { //Si existe un error, se le notifica al usuario mediante una respuesta en formato XML
            outter.write(xmlResponse("error", loginRes.get("error").toString()));
        } else {
            StringBuilder str = new StringBuilder();
            str.append("<?xml version='1.0' encoding='UTF-8>"); //Creo un XML para el usuario
            str.append("<data>");
            str.append(generateXmlTag("message", loginRes.get("message").toString())); //Le envio los campos de MENSAJE
            str.append(generateXmlTag("name", loginRes.get("name").toString())); //NOMBRE
            str.append(generateXmlTag("lastName", loginRes.get("lastName").toString())); //APELLIDO
            str.append("</data>"); //Que vienen incluidos en el HashMap del usuario
            outter.write(str.toString()); //Se lo muestro al usuario
        }
    }

    //Registra un usuario
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        helper = new XmlHelper(request.getRealPath("/"));
        response.setContentType("application/xml");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String email = request.getParameter("email"); //Obtengo los valores para poder registrar el usuario, EMAIL
        String name = request.getParameter("name"); //NOMBRE
        String lastName = request.getParameter("lastName"); //APELLIDO
        String pwd = request.getParameter("password"); //CONTRASEÑA
        HashMap<String, Object> userResponse = helper.registerUser(name, lastName, email, pwd); //Solicito la respuesta de la operacion 
        if (userResponse.get("message") != null) { //Si la operacion tiene un estado aceptable, se le envia un mensaje al usuario
            outter.print(xmlResponse("message", userResponse.get("message").toString()));
        } else {
            outter.print(xmlResponse("error", userResponse.get("error").toString())); //En el caso de error, tambien se le notifica, pero con el campo de error
        }

    }

    //Funcion para genear un encabezado de XML y contenido
    private String xmlResponse(String tag, String content) {
        return "<?xml version='1.0' encoding='UTF-8>"
                + "<" + tag + ">" + content + "</" + tag + ">";
    }

    /*
        Funcion para armar un TAG de XML simple
     */
    private String generateXmlTag(String tag, String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

}
