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
    
    //Como ya antes se dijo, esta funcion permite eliminar los errores de CORS que se pueden llegar a dar
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
        String type = request.getQueryString().split("=")[1]; //Mediante un queryString obtengo la seccion de donde se obtendran la lista de ejemplos
        StringBuilder str = new StringBuilder(); 
        List sectionItems = helper.getExampleItems(type); //Ocupando el helper, obtengo la lista de elementos de esta seccion
        if (sectionItems == null) {
            outter.write(xmlResponse("error", "Ha ocurrido un error al obtener los ejemplos")); //Muestro un error si es que ocurre
        } else {
            str.append("<?xml version='1.0' encoding='UTF-8>"); //Creo un nuevo elemento de XML
            str.append("<data>");
            for (Object item : sectionItems) { //Itero a traves de los elementos de la lista, generando una etiqueta XML con sus valores y atributos
                Element example = (Element) item;
                str.append("<item id='") //Creo el nuevo elemento
                        .append(example.getAttributeValue("id")).append("' ")
                        .append("inp1='").append(example.getAttributeValue("inp1")).append("' ")
                        .append("inp2='").append(example.getAttributeValue("inp2")).append("' ")
                        .append("inp3='").append(example.getAttributeValue("inp3")).append("' ")
                        .append("sign='").append(example.getAttributeValue("sign")).append("' ")
                        .append("type='").append(example.getAttributeValue("type")).append("' ")
                        .append(">").append(example.getText()).append("</item>");
                //Hago el mapeado de todos sus atributos y valores
            }
            str.append("</data>");
            outter.write(str.toString()); //Le regreso el XML con los elementos
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter outter = response.getWriter();
        response.setContentType("application/xml");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        String type = request.getQueryString().split("=")[1]; //Mediante el QueryString, obtengo el tipo de elemento a calcular
        StringBuilder strXml = new StringBuilder();
        int cont = 1; //Contador para el numero de Items
        switch (type) {
            case "rectas": //Si es para los ejercicios de rectas
                String inp1 = request.getParameter("inp1");
                String inp2 = request.getParameter("inp2");
                String inp3 = request.getParameter("inp3");
                String sign = request.getParameter("sign");
                String eqType = request.getParameter("eqType");
                //Se obtienen los parametros necesarios para poder trabajar
                double x = Double.parseDouble(inp1); //Se convierten llos valores a Dobles
                double y = Double.parseDouble(inp2);
                double c = inp3 != null ? Double.parseDouble(inp3) : 0; //Este bien puede ser nulo o no, dependiente del tipo de ecuacion
                if (sign.equals("-")) { //Si nuestro signo es negativo, multiplico por -1 el valor
                    y *= -1;
                }
                strXml.append("<?xml version='1.0' encoding='UTF-8>"); //Genero un nuevo XML de respuesta
                strXml.append("<values>");
                for (int i = -10; i <= 10; i++) { //Itero desde -10 hasta 10 para tener un rango aceptable de valores
                    double value = x * i + y; // Obtengo el valor de y;    y = mx + b
                    if (eqType != null && eqType.equals("general")) { //Si es de la forma general, aplico una transformacion
                        value = (-x * i / y) + (c / y); // Ax + By = C
                        //Despejo los valores de la formula general para que queden de manera punto pendiente y divido sobre el valor de Y
                    }
                    strXml.append("<").append(cont).append(">");
                    strXml.append(generateXmlTag("x", String.valueOf(i)));
                    strXml.append(generateXmlTag("y", String.valueOf(value)));
                    strXml.append("</").append(cont).append(">");
                    cont++;
                }
                strXml.append("</values>");
                outter.write(strXml.toString()); //Le regreso el XML al usuario
                break;
            case "interseccion": //En el caso de que sea Interseccion entre 2 rectas
                String eq1inp1 = request.getParameter("eq1inp1");
                String eq1inp2 = request.getParameter("eq1inp2");
                String eq1inp3 = request.getParameter("eq1inp3");
                String eq1sign = request.getParameter("eq1sign");
                String eq2inp1 = request.getParameter("eq2inp1");
                String eq2inp2 = request.getParameter("eq2inp2");
                String eq2inp3 = request.getParameter("eq2inp3");
                String eq2sign = request.getParameter("eq2sign");
                String eqsType = request.getParameter("eqsType");
                //Obtengo los parametros de las rectas
                double x1 = Double.parseDouble(eq1inp1); //Genero los valores numericos de estos mismos
                double y1 = Double.parseDouble(eq1inp2);
                double c1 = eq1inp3 != null ? Double.parseDouble(eq1inp3) : 0;
                if (eq1sign.equals("-")) {
                    y1 *= -1; //Aplico el cambio de signo si se necesita
                }

                double x2 = Double.parseDouble(eq2inp1);
                double y2 = Double.parseDouble(eq2inp2);
                double c2 = eq2inp3 != null ? Double.parseDouble(eq2inp3) : 0;
                if (eq2sign.equals("-")) {
                    y2 *= -1;
                }
                strXml.append("<?xml version='1.0' encoding='UTF-8>");
                strXml.append("<data>");
                for (int i = 1; i <= 2; i++) { //Como son 2 ecuaciones, ocupo un for para mas rapido
                    cont = 0;
                    strXml.append("<eq").append(i).append(">"); // <eq1> / <eq2>
                    for (int j = -10; j <= 10; j++) { //Aplico el mismo procedimiento que antes, desde -10 hasta 10
                        double xLin = i == 1 ? x1 : x2;
                        double yLin = i == 1 ? y1 : y2;
                        double cLin = i == 1 ? c1 : c2;
                        double value = xLin * j + yLin; //Calculo el valor en su forma punto pendiente
                        if (eqsType != null && eqsType.equals("general")) { 
                            value = (-xLin * j / yLin) + (cLin / yLin); //Si es en su forma general, la despejo a forma punto pendiente
                        }
                        strXml.append("<").append(cont).append(">");
                        strXml.append(generateXmlTag("x", String.valueOf(j)));
                        strXml.append(generateXmlTag("y", String.valueOf(value)));
                        strXml.append("</").append(cont).append(">");
                        cont++;
                    }
                    strXml.append("</eq").append(i).append(">");
                }
                //Determinantes. Para poder calcular la interseccion entre las dos rectas
                //y = mx + c  --> -mx + y = c --> -x1 + 1 = y1 -- a b c
                //x + y = c
                if (x1 != x2) {
                    double xInt = (y1 * 1 - y2 * 1) / (-x1 * 1 + x2 * 1); //En forma pendiente, Y siempre es 1
                    double yInt = (-x1 * y2 + x2 * y1) / (-x1 * 1 + x2 * 1);
                    if (eqsType != null && eqsType.equals("general")) { //En el caso de que ssea del tipo general, se ocupa la formula directamente, ya que son valores completos
                        xInt = (c1 * y2 - c2 * y1) / (x1 * y2 - x2 * y1);
                        yInt = (x1 * c2 - x2 * c1) / (x1 * y2 - x2 * y1);
                    }
                    strXml.append("<xVal>").append(String.format("%.2f", xInt)).append("</xVal>");
                    strXml.append("<yVal>").append(String.format("%.2f", yInt)).append("</yVal>");
                } else {
                    strXml.append("<error>No hay interseccion</error>"); //Si las pendientes son iguales no existe interseccion
                }

                strXml.append("</data>");
                outter.write(strXml.toString()); //Le regreso la respuesta al usuario
                break;
            case "par-perp": //Si elige la opcion de Linea Paralela y Perpendicular
                String lininp1 = request.getParameter("inp1");
                String lininp2 = request.getParameter("inp2");
                String lininp3 = request.getParameter("inp3");
                String linsign = request.getParameter("sign");
                String linType = request.getParameter("lineType");
                String linX = request.getParameter("punto1");
                String linY = request.getParameter("punto2");
                String linEqType = request.getParameter("eqType");
                //Obtengo los valores necesarios para calcular
                Double xLin = Double.parseDouble(lininp1);
                Double yLin = Double.parseDouble(lininp2);
                Double cLin = Double.parseDouble(lininp3);
                Double xLin1 = Double.parseDouble(linX);
                Double yLin1 = Double.parseDouble(linY);
                Double pendiente = 0.0;
                //Los convierto todos a Dobles para poder manipular
                if (linsign.equals("-")) {
                    yLin *= -1; //Aplico cambio de signo si es que lo necesita
                }

                pendiente = linEqType.equals("pendiente") ? xLin : xLin / yLin * -1; // Calculo la pendiente con base al tipo de linea que solicita el usuario

                if (linType.equals("perpendicular")) {
                    pendiente = -1 * (1 / pendiente); //Si es una linea perpendicular, aplico -1/m
                }

                strXml.append("<?xml version='1.0' encoding='UTF-8>");
                strXml.append("<data>");
                strXml.append("<eq1>");
                for (int i = -10; i <= 10; i++) { //Tabulo los datos de la ecuacion 1
                    double value = xLin * i + yLin; // Forma punto pendiente
                    if (linEqType.equals("general")) { //Si me la dio en forma general, ocupo los datos para calcular
                        value = (-xLin * i / yLin) + (cLin / yLin);
                    }
                    strXml.append("<").append(cont).append(">");
                    strXml.append(generateXmlTag("x", String.valueOf(i)));
                    strXml.append(generateXmlTag("y", String.valueOf(value)));
                    strXml.append("</").append(cont).append(">");
                    cont++;
                }
                strXml.append("</eq1>");
                strXml.append("<eq2>"); //Genero la nueva recta que pasa por el punto indicado
                for (int i = -10; i <= 10; i++) {
                    //Ocupo la formula Y - Y1 = m(X - X1)
                    double value = (pendiente * i) - (pendiente * xLin1) + (yLin1);
                    strXml.append("<").append(cont).append(">");
                    strXml.append(generateXmlTag("x", String.valueOf(i)));
                    strXml.append(generateXmlTag("y", String.valueOf(value)));
                    strXml.append("</").append(cont).append(">");
                    cont++;
                }
                strXml.append("</eq2>");
                strXml.append("</data>");
                outter.write(strXml.toString()); //Le regreso el XML al usuario
                break;
            default:
                outter.write(xmlResponse("message", "Aun no soportado")); //Si el usuario se equivoca de ruta o de Query String, le muestro un error
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
        System.out.println(type);
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
