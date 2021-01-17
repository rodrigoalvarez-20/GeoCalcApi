package Api;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Puntos extends HttpServlet {

    //CORS
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
        String type = request.getQueryString().split("=")[1]; //Igual que en Rectas, ocupo el QueryString para saber que ejercicio va a realizar el usuario
        StringBuilder strXml = new StringBuilder();
        if (type.equals("punto_medio")) { //Calcular el punto medio de 2 puntos y su distancia
            Double x1 = Double.parseDouble(request.getParameter("x1"));
            Double y1 = Double.parseDouble(request.getParameter("y1"));
            Double x2 = Double.parseDouble(request.getParameter("x2"));
            Double y2 = Double.parseDouble(request.getParameter("y2"));
            //Obtengo los valores
            double distancia = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)); //Calculo la distancia ocupando la formula sqrt( (X2 - X2)^2 + (Y2 - Y1)^2 )
            double pmedioX = (x2 + x1) / 2; //Calculo el punto medio de X
            double pmedioY = (y2 + y1) / 2; //Punto medio de Y
            strXml.append("<?xml version='1.0' encoding='UTF-8>");
            strXml.append("<data>");
            strXml.append("<distancia>").append(distancia).append("</distancia>");
            strXml.append("<x>").append(pmedioX).append("</x>");
            strXml.append("<y>").append(pmedioY).append("</y>");
            strXml.append("</data>");
            outter.write(strXml.toString()); //Le regreso el XML al usuario
        } else if (type.equals("two_points")) { //Recta que pasa por 2 puntos
            //Formular a ocupar
            //m = (Y2-Y1)/(X2-X1) --> Pendiente de la recta
            //Y-Y1 = m(X-X1) --> Forma punto pendiente de la nueva recta
            Double x1 = Double.parseDouble(request.getParameter("x1"));
            Double y1 = Double.parseDouble(request.getParameter("y1"));
            Double x2 = Double.parseDouble(request.getParameter("x2"));
            Double y2 = Double.parseDouble(request.getParameter("y2"));
            //Obtengo los valores
            double pendiente = (y2 - y1) / (x2 - x1); //Calculo la pendiente ocupando la formula previamente dicha
            double cVal = (pendiente * x1 * -1) + y1; //Calculo el valor de C, que es el parametro libre
            StringBuilder eqPendiente = new StringBuilder();
            StringBuilder eqGeneral = new StringBuilder();
            eqPendiente.append("y = ").append(pendiente).append("x").append(" + ").append(cVal); //Genero un string con la ecuacion en forma punto pendiente
            eqGeneral.append(pendiente).append("x - y = ").append(cVal * -1); //Genero un string con la ecuacion en forma general

            strXml.append("<?xml version='1.0' encoding='UTF-8>");
            strXml.append("<data>");
            strXml.append("<pendiente>").append(eqPendiente.toString()).append("</pendiente>");
            strXml.append("<general>").append(eqGeneral.toString()).append("</general>");
            strXml.append("<values>");
            for (int i = -10; i <= 10; i++) { //Tabulo los datos de la nueva recta
                double value = (pendiente * i) - (pendiente * x1) + (y1);
                strXml.append("<").append(i).append(">");
                strXml.append("<x>").append(i).append("</x>");
                strXml.append("<y>").append(value).append("</y>");
                strXml.append("</").append(i).append(">");
            }
            strXml.append("</values>");
            strXml.append("</data>");
            outter.write(strXml.toString()); //Regreso el XML al usuario

        } else if (type.equals("point_rect")) { //Distancia de un punto a una recta
            Double p1 = Double.parseDouble(request.getParameter("inp1"));
            Double p2 = Double.parseDouble(request.getParameter("inp2"));
            Double p3 = Double.parseDouble(request.getParameter("inp3"));
            Double x = Double.parseDouble(request.getParameter("x"));
            Double y = Double.parseDouble(request.getParameter("y"));
            String eqType = request.getParameter("eqType");
            String sign = request.getParameter("sign");
            //Obtengo los valores
            Double A, B, C;
            if (eqType.equals("general")) { //Asigno los valores para poder calcular con base al tipo de ecuacion
                A = p1;
                B = sign.equals("-") ? -1 * p2 : p2;
                C = (p3 * -1);
            } else {
                A = p1;
                B = -1.0;
                C = sign.equals("-") ? -1 * p2 : p2;
            }
            //Obtengo la distancia ocupando la formula |Ax + By + C|/sqrt(A^2 + B^2)
            double distancia = Math.abs(A * x + B * y + C) / Math.sqrt(Math.pow(A, 2) + Math.pow(B, 2));
            if (sign.equals("-")) {
                p2 *= -1; //Cambio signo de ser necesario
            }
            strXml.append("<?xml version='1.0' encoding='UTF-8>");
            strXml.append("<data>");
            strXml.append("<distancia>").append(distancia).append("</pendiente>");
            strXml.append("<values>");
            int cont = 1;
            for (int i = -10; i <= 10; i++) { //Tabulo los datos para hacer la recta
                double value = p1 * i + p2;
                if (eqType.equals("general")) {
                    value = (-p1 * i / p2) + (p3 / p2);
                }
                strXml.append("<").append(cont).append(">");
                strXml.append(generateXmlTag("x", String.valueOf(i)));
                strXml.append(generateXmlTag("y", String.valueOf(value)));
                strXml.append("</").append(cont).append(">");
                cont++;
            }
            strXml.append("</values>");
            strXml.append("</data>");
            outter.write(strXml.toString()); //Le regreso el XML al usuario

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
