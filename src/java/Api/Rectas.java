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
        String type = request.getQueryString().split("=")[1];
        StringBuilder strXml = new StringBuilder();
        int cont = 1;
        switch (type) {
            case "rectas":
                String inp1 = request.getParameter("inp1");
                String inp2 = request.getParameter("inp2");
                String inp3 = request.getParameter("inp3");
                String sign = request.getParameter("sign");
                String eqType = request.getParameter("eqType");
                double x = Double.parseDouble(inp1);
                double y = Double.parseDouble(inp2);
                double c = inp3 != null ? Double.parseDouble(inp3) : 0;
                if (sign.equals("-")) {
                    y *= -1;
                }
                strXml.append("<?xml version='1.0' encoding='UTF-8>");
                strXml.append("<values>");
                for (int i = -10; i <= 10; i++) {
                    double value = x * i + y;
                    if (eqType != null && eqType.equals("general")) {
                        value = (-x * i / y) + (c / y);
                    }
                    strXml.append("<").append(cont).append(">");
                    strXml.append(generateXmlTag("x", String.valueOf(i)));
                    strXml.append(generateXmlTag("y", String.valueOf(value)));
                    strXml.append("</").append(cont).append(">");
                    cont++;
                }
                strXml.append("</values>");
                outter.write(strXml.toString());
                break;
            case "interseccion":
                String eq1inp1 = request.getParameter("eq1inp1");
                String eq1inp2 = request.getParameter("eq1inp2");
                String eq1inp3 = request.getParameter("eq1inp3");
                String eq1sign = request.getParameter("eq1sign");
                String eq2inp1 = request.getParameter("eq2inp1");
                String eq2inp2 = request.getParameter("eq2inp2");
                String eq2inp3 = request.getParameter("eq2inp3");
                String eq2sign = request.getParameter("eq2sign");
                String eqsType = request.getParameter("eqsType");

                double x1 = Double.parseDouble(eq1inp1);
                double y1 = Double.parseDouble(eq1inp2);
                double c1 = eq1inp3 != null ? Double.parseDouble(eq1inp3) : 0;
                if (eq1sign.equals("-")) {
                    y1 *= -1;
                }

                double x2 = Double.parseDouble(eq2inp1);
                double y2 = Double.parseDouble(eq2inp2);
                double c2 = eq2inp3 != null ? Double.parseDouble(eq2inp3) : 0;
                if (eq2sign.equals("-")) {
                    y2 *= -1;
                }
                strXml.append("<?xml version='1.0' encoding='UTF-8>");
                strXml.append("<data>");
                for (int i = 1; i <= 2; i++) {
                    cont = 0;
                    strXml.append("<eq").append(i).append(">");
                    for (int j = -10; j <= 10; j++) {
                        double xLin = i == 1 ? x1 : x2;
                        double yLin = i == 1 ? y1 : y2;
                        double cLin = i == 1 ? c1 : c2;
                        double value = xLin * j + yLin;
                        if (eqsType != null && eqsType.equals("general")) {
                            value = (-xLin * j / yLin) + (cLin / yLin);
                        }
                        strXml.append("<").append(cont).append(">");
                        strXml.append(generateXmlTag("x", String.valueOf(j)));
                        strXml.append(generateXmlTag("y", String.valueOf(value)));
                        strXml.append("</").append(cont).append(">");
                        cont++;
                    }
                    strXml.append("</eq").append(i).append(">");
                }
                //Determinantes
                //y = mx + c  --> -mx + y = c --> -x1 + 1 = y1 -- a b c
                //x + y = c
                if (x1 != x2) {
                    double xInt = (y1 * 1 - y2 * 1) / (-x1 * 1 + x2 * 1);
                    double yInt = (-x1 * y2 + x2 * y1) / (-x1 * 1 + x2 * 1);
                    if (eqsType != null && eqsType.equals("general")) {
                        xInt = (c1 * y2 - c2 * y1) / (x1 * y2 - x2 * y1);
                        yInt = (x1 * c2 - x2 * c1) / (x1 * y2 - x2 * y1);
                    }
                    strXml.append("<xVal>").append(String.format("%.2f", xInt)).append("</xVal>");
                    strXml.append("<yVal>").append(String.format("%.2f", yInt)).append("</yVal>");
                } else {
                    strXml.append("<error>No hay interseccion</error>");
                }

                strXml.append("</data>");
                outter.write(strXml.toString());
                break;
            case "par-perp":
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
            //Añadir dato
            if (helper.addExampleItem(id, inp1, inp2, inp3, type, sign, text, section)) {
                outter.write(xmlResponse("message", "Se ha agregado correctamente"));
            } else {
                outter.write(xmlResponse("error", "Ha ocurrido un error al agregar el item"));
            }
        } else {
            //Actualizar dato
            if (helper.updateExampleItem(id, inp1, inp2, inp3, type, sign, text, section)) {
                outter.write(xmlResponse("message", "Se ha actualizado correctamente"));
            } else {
                outter.write(xmlResponse("error", "Ha ocurrido un error al actualizar el registro"));
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
