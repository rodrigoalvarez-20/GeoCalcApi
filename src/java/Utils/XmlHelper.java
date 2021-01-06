package Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

public class XmlHelper {

    private final String fileName = "geocalc.xml";
    private final SAXBuilder builder = new SAXBuilder();
    private File xmlFile = null;
    private int maxFileSize = 10 * 1024 * 1024;
    private int maxMemSize = 12 * 1024;
    private String path = "";

    public XmlHelper(String path) {
        this.path = path;
        this.xmlFile = new File(path + this.fileName);
    }

    private boolean isUserRegistered(String email) {
        try {
            Document doc = (Document) builder.build(this.xmlFile);
            Element root = doc.getRootElement();
            Element usersDict = root.getChild("users");
            List users = usersDict.getChildren();
            for (Object user : users) {
                Element userObj = (Element) user;
                if (userObj.getText().equals(email)) {
                    return true;
                }
            }
            return false;
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public HashMap<String, Object> registerUser(String name, String lastName, String email, String pwd) {
        HashMap<String, Object> res = new HashMap();
        if (isUserRegistered(email)) {
            res.put("error", "El usuario ya existe");
        } else {

            Element toAdd = new Element("user");
            toAdd.setAttribute("name", name);
            toAdd.setAttribute("last_name", lastName);
            toAdd.setAttribute("password", pwd);
            toAdd.setText(email);

            try {
                Document doc = (Document) builder.build(this.xmlFile);
                Element root = doc.getRootElement();
                Element usersDict = root.getChild("users");

                usersDict.addContent(toAdd);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                    xmlOutput.output(doc, writer);
                    writer.flush();
                    res.put("message", "Se ha agregado el usuario");
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                res.put("error", "Ha ocurrido un error al agregar el usuario");
            }
        }
        return res;
    }

    public HashMap<String, Object> loginUser(String email, String pwd) {
        HashMap<String, Object> res = new HashMap();
        if (!isUserRegistered(email)) {
            res.put("error", "El usuario no esta registrado");
        } else {
            try {
                Document doc = (Document) builder.build(this.xmlFile);
                Element root = doc.getRootElement();
                Element usersNode = root.getChild("users");
                List users = usersNode.getChildren();
                for (Object user : users) {
                    Element userObj = (Element) user;
                    if (userObj.getText().equals(email) && userObj.getAttributeValue("password").equals(pwd)) {
                        res.put("message", "Se ha iniciado sesion correctamente");
                        res.put("name", userObj.getAttributeValue("name"));
                        res.put("lastName", userObj.getAttributeValue("last_name"));
                    }
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                res.put("error", "Ha ocurrido un error al buscar el usuario");
            }
        }
        return res;
    }

}
