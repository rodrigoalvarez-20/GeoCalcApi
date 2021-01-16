package Api;

import Utils.XmlHelper;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Puntos extends HttpServlet {

    private XmlHelper helper = null;

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        response.setContentType("application/xml");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String type = request.getQueryString().split("=")[1];
        StringBuilder strXml = new StringBuilder();
        if (type.equals("punto_medio")) {
            Double x1 = Double.parseDouble(request.getParameter("x1"));
            Double y1 = Double.parseDouble(request.getParameter("y1"));
            Double x2 = Double.parseDouble(request.getParameter("x2"));
            Double y2 = Double.parseDouble(request.getParameter("y2"));

            double distancia = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
            double pmedioX = (x2 + x1) / 2;
            double pmedioY = (y2 + y1) / 2;
            strXml.append("<?xml version='1.0' encoding='UTF-8>");
            strXml.append("<data>");
            strXml.append("<distancia>").append(distancia).append("</distancia>");
            strXml.append("<x>").append(pmedioX).append("</x>");
            strXml.append("<y>").append(pmedioY).append("</y>");
            strXml.append("</data>");
            outter.write(strXml.toString());

        } else {
            outter.write(xmlResponse("message", "Aun no soportado"));
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
