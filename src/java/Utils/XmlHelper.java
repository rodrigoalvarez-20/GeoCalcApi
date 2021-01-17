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

/*
    Clase auxiliar que permite la maniupulacion de archivos XML
*/

public class XmlHelper {

    private final String fileName = "geocalc.xml"; //Nombre de nuestro archivo XML
    private final SAXBuilder builder = new SAXBuilder(); //SAXBuilder para las operaciones de archivos
    private File xmlFile = null; //Variable para almacenar el archivo
    private String path = ""; //Path base

    /*
        Constructor que permite crear una nueva instancia de la clase, al cual se le pasa el PATH actual de la aplicacion para poder localizar el archivo de configuracion
    */
    public XmlHelper(String path) {
        this.path = path;
        this.xmlFile = new File(path + this.fileName);
    }

    /*
        Funcion que verifica si un usuario ya ha sido registrado en el archivo o no
    */
    private boolean isUserRegistered(String email) {
        try {
            Document doc = (Document) builder.build(this.xmlFile);
            Element root = doc.getRootElement();
            Element usersDict = root.getChild("users"); //Busca en el apartado de users
            List users = usersDict.getChildren();
            for (Object user : users) {
                Element userObj = (Element) user;
                if (userObj.getText().equals(email)) { //Compara los emails, ya que son unicos
                    return true; //Si encuentra coincidencia arroja true
                }
            }
            return false;
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }

    /*
        Funcion que permite reigistrar un usuario, como parametros tennemos el nombre, apellido, email y contrseña
        Deuelve un HashMap<String, Object> que es lo mas cercano a un JSON, dependiendo del estado de la operacion
    */
    public HashMap<String, Object> registerUser(String name, String lastName, String email, String pwd) {
        HashMap<String, Object> res = new HashMap();
        if (isUserRegistered(email)) { //Busca si el usuario ya esta registrado en la aplicacion
            res.put("error", "El usuario ya existe"); //Añade un elemento de error al Mapa
        } else {
            //Si no esta registrado, entonces crea un nuevo elemento y lo inserta en el archivo
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
                    res.put("message", "Se ha agregado el usuario"); //Añade un elemento de exito al Mapa
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                res.put("error", "Ha ocurrido un error al agregar el usuario");
            }
        }
        return res; //Regresa el mapa. Ojo, este mapa solamente puede tener 1 de las 2 opciones Error o Exito en la operacion
    }

    /*
        Funcion que permite loggear al usuario al sistema, devuelve un HashMap que tendrá un estado de aceptacion o de error
    */
    public HashMap<String, Object> loginUser(String email, String pwd) {
        HashMap<String, Object> res = new HashMap();
        if (!isUserRegistered(email)){ //El usuario no esta registrado en la base de datos
            res.put("error", "El usuario no esta registrado");
        } else {
            try {
                Document doc = (Document) builder.build(this.xmlFile);
                Element root = doc.getRootElement();
                Element usersNode = root.getChild("users"); //Obtiene los nodos del usuario
                List users = usersNode.getChildren(); //Los convierte a lista para iterar
                for (Object user : users) {
                    Element userObj = (Element) user;
                    if (userObj.getText().equals(email) && userObj.getAttributeValue("password").equals(pwd)) { //Compara email y password del archivo de configuracion con los que ha ingresado el usuario
                        res.put("message", "Se ha iniciado sesion correctamente");
                        res.put("name", userObj.getAttributeValue("name")); //Regreso los valores de Nombre
                        res.put("lastName", userObj.getAttributeValue("last_name")); //Y de apellidos, para mostrarlos en la aplicacion
                    }
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                res.put("error", "Ha ocurrido un error al buscar el usuario"); //Regreso un error
            }
        }
        return res;
    }

    /*
        Funcion que permite obtener la lista de ejemplos con base a la seccion introducida
    */
    public List getExampleItems(String section) {
        try {
            Document doc = (Document) builder.build(this.xmlFile);
            Element root = doc.getRootElement();
            Element appData = root.getChild("application"); //Obtene los nodos de la seccion de aplicacion
            Element sectionItems = appData.getChild(section); //Obtiene los items en dicha seccion
            List items = sectionItems.getChildren(); //Los convierte a lista y los regresa
            return items;
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return null;
        }
    }

    /*
        Funcion que permite eliminar un elemento de una seccion del archivo XML
        Regresa un HashMap con la informacion de la accion
    */
    public HashMap<String, Object> deleteItemFromExamples(String id, String section) {
        HashMap<String, Object> res = new HashMap();
        try {
            Document document = (Document) builder.build(this.xmlFile);
            Element rootNode = document.getRootElement();
            Element appData = rootNode.getChild("application");
            Element sectionItems = appData.getChild(section);
            List items = sectionItems.getChildren();
            boolean found = false; //Variable para saber si se ha encontrado el objeto o no
            for (Object node : items) {
                Element elemento = (Element) node;
                if (elemento.getAttributeValue("id").equals(id)) { //Busco por id el elemento a eliminar
                    sectionItems.removeContent(elemento); //Lo elimino
                    found = true; //Indico que ya lo encontre
                    break; //Termino esta ejecucion
                }
            }
            if (found) { //Si lo he encontrado
                XMLOutputter xmlOutput = new XMLOutputter(); 
                xmlOutput.setFormat(Format.getPrettyFormat());
                try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                    xmlOutput.output(document, writer); //Genero la nueva version del archivo con el elemento ya eliminado
                    writer.flush();
                    res.put("message", "Se ha eliminado el ejemplo"); //Regreso un mensaje de aceptacion
                }
            } else {
                res.put("error", "No se ha encontrado el ejemplo en el archivo"); //No se ha encontrado, devuelvo un error
            }
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            res.put("error", "Ha ocurrido un error al eliminar el ejemplo"); //Devuelvo un error
        }
        return res;
    }

    /*
        Funcion que permite añadir un nuevo ejemplo a una seccion determinada
        Sele pasan los parametros a guardar y la seccion donde se quiere incluir
    */
    public boolean addExampleItem(String id, String inp1, String inp2, String inp3, String type, String sign, String label, String section) {
        Element toAdd = new Element("item"); //Genero un nuevo elemento y lleno sus campos
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
            Element sectItems = appData.getChild(section); //Busco la seccion donde han indicado
            sectItems.addContent(toAdd); //Lo inserto
            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                xmlOutput.output(doc, writer); //Escribo la nueva version del archivo
                writer.flush(); //Finalizo
                return true;
            }
        } catch (JDOMException | IOException ex) {
            System.out.println(ex.getMessage());
            return false; //Regreso un estatus de error
        }
    }

    /*
        Actualizar un ejemplo preexistente
        Se le pasan todos los parametros, el id a buscar y la seccion de donde es el ejemplo
    */
    public boolean updateExampleItem(String id, String inp1, String inp2, String inp3, String type, String sign, String label, String section) {
        Element edited = searchElementAndRemove(id, section); //Busco y elimino el elemento a modificar. Adicionalmente, regreso una copia para poder modifcar sus atributos
        if (edited == null) {
            return false; //Regreso un estado de error / no valido
        } else {
            edited.setAttribute("inp1", inp1); //Actualizo todos los valores del elemento
            edited.setAttribute("inp2", inp2);
            edited.setAttribute("inp3", inp3);
            edited.setAttribute("sign", sign);
            edited.setAttribute("type", type);
            edited.setText(label);
            try {
                Document doc = (Document) builder.build(this.xmlFile);
                Element root = doc.getRootElement();
                Element appData = root.getChild("application"); //Obtengo el nodo de applicacion
                Element sectItems = appData.getChild(section); //Los nodos de la seccion deseada
                sectItems.addContent(edited); //Inserto el nodo modificado
                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat());
                try (FileWriter writer = new FileWriter(this.path + this.fileName)) {
                    xmlOutput.output(doc, writer); //Creo la nueva version
                    writer.flush();
                    return true; //Regreso un estado valido
                }
            } catch (JDOMException | IOException ex) {
                System.out.println(ex.getMessage());
                return false; //Regreso un error
            }
        }
    }

    /*
        Funcion auxiliar para buscar y eliminar un elemento del archivo
        Se le pasan el id a buscar y la seccion a ocupar
        Esta funcion hace lo mismo que "eliminarElemento", pero a diferencia de ello, regresa una copia
    */
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
