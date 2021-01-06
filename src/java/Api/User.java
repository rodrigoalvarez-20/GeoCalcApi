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

    private XmlHelper helper = null;

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
        helper = new XmlHelper(request.getRealPath("/"));
        response.setContentType("application/xml");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
        String email = request.getParameter("email");
        String pwd = request.getParameter("password");
        HashMap<String, Object> loginRes = helper.loginUser(email, pwd);
        if (loginRes.get("error") != null) {
            outter.write(xmlResponse("error", loginRes.get("error").toString()));
        } else {
            StringBuilder str = new StringBuilder();
            str.append("<?xml version='1.0' encoding='UTF-8>");
            str.append("<data>");
            str.append(generateXmlTag("message", loginRes.get("message").toString()));
            str.append(generateXmlTag("name", loginRes.get("name").toString()));
            str.append(generateXmlTag("lastName", loginRes.get("lastName").toString()));
            str.append("</data>");
            outter.write(str.toString());
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
        String email = request.getParameter("email");
        String name = request.getParameter("name");
        String lastName = request.getParameter("lastName");
        String pwd = request.getParameter("password");
        HashMap<String, Object> userResponse = helper.registerUser(name, lastName, email, pwd);
        if (userResponse.get("message") != null) {
            outter.print(xmlResponse("message", userResponse.get("message").toString()));
        } else {
            outter.print(xmlResponse("error", userResponse.get("error").toString()));
        }

    }

    private String xmlResponse(String tag, String content) {
        return "<?xml version='1.0' encoding='UTF-8>"
                + "<" + tag + ">" + content + "</" + tag + ">";
    }

    private String generateXmlTag(String tag, String content) {
        return "<" + tag + ">" + content + "</" + tag + ">";
    }

}
