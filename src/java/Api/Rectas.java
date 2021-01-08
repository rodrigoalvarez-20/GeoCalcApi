package Api;

import Utils.XmlHelper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jdom.Element;

public class Rectas extends HttpServlet {

    private XmlHelper helper = null;

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        resp.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        helper = new XmlHelper(request.getRealPath("/"));
        response.setContentType("application/xml");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
        String type = request.getQueryString().split("=")[1];
        StringBuilder str = new StringBuilder();
        List sectionItems = helper.getExampleItems(type);
        if (sectionItems == null) {
            outter.write(xmlResponse("error", "Ha ocurrido un error al obtener los ejemplos"));
        } else {
            str.append("<?xml version='1.0' encoding='UTF-8>");
            str.append("<data>");
            for (Object item : sectionItems) {
                Element example = (Element) item;
                str.append("<item id='")
                        .append(example.getAttributeValue("id")).append("' ")
                        .append("inp1='").append(example.getAttributeValue("inp1")).append("' ")
                        .append("inp2='").append(example.getAttributeValue("inp2")).append("' ")
                        .append("inp3='").append(example.getAttributeValue("inp3")).append("' ")
                        .append("sign='").append(example.getAttributeValue("sign")).append("' ")
                        .append("type='").append(example.getAttributeValue("type")).append("' ")
                        .append(">").append(example.getText()).append("</item>");
            }

            str.append("</data>");
            outter.write(str.toString());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        response.setContentType("application/xml");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String inp1 = request.getParameter("inp1");
        String inp2 = request.getParameter("inp2");
        String sign = request.getParameter("sign");
        String type = request.getQueryString().split("=")[1];
        StringBuilder str = new StringBuilder();
        int cont = 1;
        double secValue = Double.parseDouble(inp2);
        if (sign.equals("-")) {
            secValue *= -1;
        }
        switch (type) {
            case "linear-slope":
                str.append("<?xml version='1.0' encoding='UTF-8>");
                str.append("<values>");
                for (int i = -10; i <= 10; i++) {
                    double value = Double.parseDouble(inp1) * i + secValue;
                    str.append("<").append(cont).append(">");
                    str.append(generateXmlTag("x", String.valueOf(i)));
                    str.append(generateXmlTag("y", String.valueOf(value)));
                    str.append("</").append(cont).append(">");
                    cont++;
                }
                str.append("</values>");
                outter.write(str.toString());
                break;
            case "linear-general":
                String inp3 = request.getParameter("inp3");
                Double v1 = Double.parseDouble(inp1);
                Double v2 = secValue;
                Double v3 = Double.parseDouble(inp3);
                str.append("<?xml version='1.0' encoding='UTF-8>");
                str.append("<values>");
                for (int i = -10; i <= 10; i++) {
                    double value = (-1 * v1 * i / v2) + (v3 / v2);
                    str.append("<").append(cont).append(">");
                    str.append(generateXmlTag("x", String.valueOf(i)));
                    str.append(generateXmlTag("y", String.valueOf(value)));
                    str.append("</").append(cont).append(">");
                    cont++;
                }
                str.append("</values>");
                outter.write(str.toString());
                break;
            default:
                outter.write(xmlResponse("message", "Aun no soportado"));
                break;
        }

    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        helper = new XmlHelper(request.getRealPath("/"));
        response.setContentType("application/xml");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept");
        String id = request.getParameter("id");
        String type = request.getParameter("type");
        HashMap<String, Object> delRes = helper.deleteItemFromExamples(id, type);
        if (delRes.containsKey("error")) {
            outter.write(xmlResponse("error", delRes.get("error").toString()));
        } else {
            outter.write(xmlResponse("message", delRes.get("message").toString()));
        }

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        helper = new XmlHelper(request.getRealPath("/"));
        response.setContentType("application/xml");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String id = request.getParameter("id");
        String inp1 = request.getParameter("inp1");
        String inp2 = request.getParameter("inp2");
        String inp3 = request.getParameter("inp3");
        String sign = request.getParameter("sign");
        String text = request.getParameter("label");
        String type = request.getParameter("type");
        String section = request.getParameter("section");
        String isForAdd = request.getParameter("isForAdd");
        if (isForAdd != null) {
            //Actualizar dato
        } else {
            //Añadir un nuevo dato
            if (helper.addExampleItem(id, inp1, inp2, inp3, type, sign, text, section)) {
                outter.write(xmlResponse("message", "Se ha agregado correctamente"));
            } else {
                outter.write(xmlResponse("error", "Ha ocurrido un error al agregar el item"));
            }
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
