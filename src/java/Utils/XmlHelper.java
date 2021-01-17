package Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

    public List getExampleItems(String section) {
        try {
            Document doc = (Document) builder.build(this.xmlFile);
            Element root = doc.getRootElement();
            Element appData = root.getChild("application");
            Element sectionItems = appData.getChild(section);
            List items = sectionItems.getChildren();
            return items;
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }

        //return examples;
    }

    public HashMap<String, Object> deleteItemFromExamples(String id, String section) {
        HashMap<String, Object> res = new HashMap();
        try {
            Document document = (Document) builder.build(this.xmlFile);
            Element rootNode = document.getRootElement();
            Element appData = rootNode.getChild("application");
            Element sectionItems = appData.getChild(section);
            List items = sectionItems.getChildren();
            boolean found = false;
            for (Object node : items) {
                Element elemento = (Element) node;
                if (elemento.getAttributeValue("id").equals(id)) {
                    sectionItems.removeContent(elemento);
                    found = true;
                    break;
                }
            }
            if (found) {
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                    xmlOutput.output(document, writer);
                    writer.flush();
                    res.put("message", "Se ha eliminado el ejemplo");
                }
            } else {
                res.put("error", "No se ha encontrado el ejemplo en el archivo");
            }
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            res.put("error", "Ha ocurrido un error al eliminar el ejemplo");
        }
        return res;
    }

    public boolean addExampleItem(String id, String inp1, String inp2, String inp3, String type, String sign, String label, String section) {
        Element toAdd = new Element("item");
        toAdd.setAttribute("id", id);
        toAdd.setAttribute("inp1", inp1);
        toAdd.setAttribute("inp2", inp2);
        toAdd.setAttribute("inp3", inp3);
        toAdd.setAttribute("sign", sign);
        toAdd.setAttribute("type", type);
        toAdd.setText(label);
        try {
            Document doc = (Document) builder.build(this.xmlFile);
            Element root = doc.getRootElement();
            Element appData = root.getChild("application");
            Element sectItems = appData.getChild(section);
            sectItems.addContent(toAdd);
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                xmlOutput.output(doc, writer);
                writer.flush();
                return true;
            }
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    public boolean updateExampleItem(String id, String inp1, String inp2, String inp3, String type, String sign, String label, String section) {
        Element edited = searchElementAndRemove(id, section);
        if (edited == null) {
            return false;
        } else {
            edited.setAttribute("inp1", inp1);
            edited.setAttribute("inp2", inp2);
            edited.setAttribute("inp3", inp3);
            edited.setAttribute("sign", sign);
            edited.setAttribute("type", type);
            edited.setText(label);
            try {
                Document doc = (Document) builder.build(this.xmlFile);
                Element root = doc.getRootElement();
                Element appData = root.getChild("application");
                Element sectItems = appData.getChild(section);
                sectItems.addContent(edited);
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                    xmlOutput.output(doc, writer);
                    writer.flush();
                    return true;
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                return false;
            }
        }
    }

    private Element searchElementAndRemove(String id, String section) {
        try {
            Document document = (Document) builder.build(this.xmlFile);
            Element rootNode = document.getRootElement();
            Element appData = rootNode.getChild("application");
            Element sectionItems = appData.getChild(section);
            List items = sectionItems.getChildren();
            Element toView = null;
            for (Object node : items) {
                Element elemento = (Element) node;
                if (elemento.getAttributeValue("id").equals(id)) {
                    toView = elemento;
                    sectionItems.removeContent(elemento);
                    XMLOutputter xmlOutput = new XMLOutputter();
                    xmlOutput.setFormat(Format.getPrettyFormat());
                    try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                        xmlOutput.output(document, writer);
                        writer.flush();
                    }
                    break;
                }
            }
            return toView;
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

}
