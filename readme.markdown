# Owl2Java readme 

The readme is also available as *readme.html*, which has more pretty formatting.

### Summary 

Owl2Java is a Java code generator, which generates a convenient API to work with data in an OWL ontology. It makes it possible to write expressive and easy-to-follow code that works with RDF/OWL ontologies, hiding the peculiarities of working with such an ontology and a library that accesses it. The code generator generates a Java class for each OWL class in the ontology, with static methods to retrieve and iterate over individuals, and with getters and setters for all of the class's properties Tools to enrich the structure of an OWL ontology (i.e. class and property definitions) are also included in the package. 

### Where to get it 

Owl2Java is available at <http://github.com/piscisaureus/owl2java>. 

## Background 

At the TU Delft we're trying to create a simulation that uses a semantic data set developed using SemanticMediaWiki. SemanticMediaWiki (or any other distributed ontology editor for that matter) makes it possible to collaboratively develop a knowledge body that can be used for many purposes. The wiki allows to export (a part of) the data in it as an OWL/RDF ontology, which can be used by other applications. 

The simulation model itself will be written in Java. However using an ontology in a strongly typed programming language like Java isn't really straightforward; Libraries that access instance data in an ontology do exist (like Jena), but conceptually simple operations often require verbose code. For example, to access a property of an OWL instance requires code to load the instance, then load the property definition, fetch the property value and finally cast it to an appropriate type. To do this right the programmer needs to know exactly how the property is defined within the ontology. Furthermore, the verbosity of this code makes it difficult to follow for a human reader. 

To make a developer's life a little more easy, Jena and many other libraries also provide the possibility to create 'wrapper classes' that allow an individual of a particular OWL class to be represented by an instance of a normal Java class, so all peculiarities of working with an ontology can be hidden within the wrapper class. The wrapper class exposes an easy-to-use and Java-like interface to the 'real' application. Writing these wrapper classes however can be a lot of work by itself when there are many OWL classes in the ontology; when an ontology tends to evolve this becomes an even bigger problem, because the wrapper classes need to be updated manually to keep them in sync with the ontology. 

This is where Owl2Java helps: Owl2Java can generate these wrapper classes automatically. When used properly, it generates a complete and consistent interface that works for all classes within an OWL ontology. And, provided that the user doesn't change the generated code, it can always be re-run after the ontology has been updated. The generated code uses Jena underneath; when desired, advanced Jena features such as performing SPARQL queries can still be used. 

The other issue that we ran into at the TU Delft is that the ontology exported by SemanticMediaWiki isn't always complete in terms of structure. Specifically, certain aspects of OWL properties such as range, domain and functional-ness aren't defined in the wiki export. Therefore we included some tools to enrich an ontology's structure by examining individuals. Imperfect as the results may be, it generally finds a structure that's *almost* right, limiting the amount of manual editing required. 

## Using Owl2Java 

Writing an application using Owl2Java generally consists of three stages. First, the ontology's structure needs to be checked and optionally fixed. The second step is to generate the wrapper classes, which can then be used to write an appliction. 

### Structuring the ontology 

In order for the code generator to work properly you must make sure that the ontology has a 'complete' structure: 

*   Classes must be defined in the ontology; Owl2Java generates a java class for each of them. 
*   Properties must be defined in the ontology; Owl2Java generates getter and setter methods for them. 
*   Properties need to have one or multiple classes in its domain; these are the classes that the get/setters will be added to. 
*   Properties need to have a range (a class or a data type), otherwise the getters will return an object of type owl:Thing. Properties may have multiple ranges, but Owl2Java doesn't support complex constructs (i.e. anonymous classes) here. 
*   If you want to have a singular getter, i.e. getSomething() instead of listSomething(), you'll need to make the property functional or set is maximum cardinality to one. 

Not all ontologies may have a structure as complete as this. As said, at the TU Delft we work with ontologies created with SemanticMediaWiki, which defines classes and properties but doesn't add any information about domains, ranges and does not declare any property functional. To help fixing this some structure-finding tools are included with Owl2Java. These are: 

*   *PropertyDomainInferer*  
    Find the domain(s) for a property by looking at the instances that use this property. Existing domains are retained, unless a superclass of the existing domain is found. You may need to run ThingExtender (see below) first. 
*   *PropertyRangeInferer*  
    Find the range(s) for a property by looking at what the property refers to when used by instances. Existing ranges are kept, unless a superclass of an existing range class is found. You may need to run ThingExtender and PropertyRangeSimplifier first. 
*   *FunctionalPropertyInferer*  
    Look for properties that are never used more than once per instance, and assume these properties are functional. This may lead to false positives; review the output ontology after running it. Existing functional properties are retained. 
*   *PropertyRangeSimplifier*  
    Simplify range definitions so the code generator doesn't choke on it. Some ontology editors (Protégé for example) use complex structures such as anonymous intersection/union classes to denote that a class property may have multiple ranges. This tool simply converts anything crazy to a flat list of named classes, which the code generator can work with. Generally you'll want to use this just before invoking the class generator to make sure it is run every time after the ontology was edited. Also run it before invoking PropertyRangeInferer when the ontology already has some ranges defined. 
*   *ThingExtender*  
    Make sure all classes (indirectly) extend owl:Thing. PropertyDomainInferer and PropertyRangeInferer require that owl:Thing is always at the top of the inheritance chain to function properly. Use ThingExtender before running the range/domain inferrers just to be sure. 

All these tools are used in a similar way: 

    IOntologyPreprocessor structureTool = new StructureTool( [options…] )
    structureTool.process( OntModel ontology );
    

Suppose you have an ontology but the structure is incomplete, and you want to fix it up. Create a small program that loads and ontology, runs some of the structure finding tools and then writes it back to another ontology. 

    class StructureFinder {
        public static void main(String[] args) {
            try {
                // Load the ontology, use Jena or this shortcut
                OntModel ontModel = OntologyUtils.loadOntology("file:ontology-raw.owl");
    
                // These are the tools that we will use to structure the ontology
                IOntologyProcessor[] structurizers = {
                    new ThingExtender(),
                    new FunctionalPropertyInferer(),
                    new PropertyDomainInferer(),
                    new PropertyRangeSimplifier(),
                    new PropertyRangeInferer()
                };
    
                // Run all the processors
                for (IOntologyProcessor preprocessor : structurizers) {
                    preprocessor.process(ontModel);
                }
    
                // Now we're going to save the structured ontology for manual review
                OntologyUtils.saveOntologyRdf(ontModel, "ontology-reviewme.owl");
            } catch (Exception e) {
                …
            }
        }
    }
    

### Generate wrapper classes 

Generating the java classes should be rather straightforward; it's as simple as loading an ontology, choose in which package and directory the generated classes should be placed and then call the code generator. You should run the range simplifier (see above) just before invoking the class generator to make sure the code generator doesn't choke on range definitions that are too complex for it to handle. 

    class ClassGenerator {
        public static void main(String[] args) {
            try {
                // Load ontology, use Jena or a shorcut like this
                OntModel ontModel = OntologyUtils.loadOntology("file:ontology.owl");
    
                // Simplify the definition of property ranges
                // This is necessary because Owl2Java chokes on complex range
                // definitions (i.e. those containing anonymous classes)
                (new PropertyRangeSimplifier()).process(ontModel);
    
                // Generate classes that provide access to ontology instances
                JenaGenerator generator = new JenaGenerator();
                generator.generate(ontModel, "src", "com.yourorg.ontology");
    
            } catch (Exception e) {
                …
            }
        }
    }
    

### Use the generated classes in your application 

After generating code to work with the ontology, you may start building your own application. It is recommended that you do not make any modifications to the generated code. This will allow you to just re-run the code generator when something in the ontology changes without losing your own code. 

#### Initializing the wrapper classes 

At the start of your application, you need register all generated classes with Jena. It's as simple as: 

    Factory.registerCustomClasses();
    

Obviously you need to load an ontology to work with. Use Jena for it. You may optionally set a 'default' ontology that is used when creating/loading/listing ontology instances. This is recommended; if you don't do that you need to supply the ontology every time you're loading or creating instances. 

    // Load the ontology
    OntModel ontModel = OntologyUtils.loadOntology("file:resources/demo/industries-application.owl");
    
    // Set default ontology to load instances from
    Factory.setDefaultModel(ontModel);
    

After that, you can work with ontology instances almost like normal java objects. What you can expect the generated classes to look like will be explained next. 

#### Class hierarchy 

As said, Owl2Java generates a class for each class defined in the ontology. 

However Java does not support multiple inheritance like Owl does, therefore generated Java classes do not extend their OWL 'parent' - they only extend the generic IndividualImpl class from Jena. However for each class there is also an interface generated, which *does* reflect the OWL inheritance chain. This is what the wrapper class/interface definition for a particular OWL will look like: 

    public interface IMyClass extends Individual, IMyAncestor, IThing {
        …
    }
    public class MyClass extends IndividualImpl implements IMyClass {
        …
    }
    

#### Working with instances 

Generated classes have a number of static methods to load, delete, list and iterate over instances of a particular OWL class. The method names should be self-explaining; however some details may require explanation: 

*   The OntModel parameter is always optional; if not supplied, the default ontology set with *Factory.setDefaultModel()* is used. 
*   When creating an instance, the URI parameter may be omitted; an url for the instance will then be generated. 
*   Delete() is a static method and requires you to give it the url of the instance to be deleted. To remove an instance that has been 'wrapped' with get(), use the non-static remove() method. 
*   The iterate(), list() and count() methods optionally take a parameter named *direct*. When it is set to true, these functions consider only direct instances of this class, ignoring instances of subclasses. If *direct* is set to false or omitted, subclass instances are listed, iterated, counted too. 

#####  

    public static MyClass create( [String URI], [OntModel ontology] )
    public static boolean exists( String URI, [OntModel ontology] )
    public static MyClass get( String URI, [OntModel ontology] )
    public static void delete( String URI, [OntModel ontology] )
    
    public static Iterator<MyClass> iterate( [boolean direct], [OntModel ontology] )
    public static List<MyClass> list( [boolean direct], [OntModel ontology] )
    public static int count( [boolean direct], [OntModel ontology] )
    
    public boolean remove()


#### Working with properties 

The generated classes have methods to work with the properties of an OWL individual. What methods are available for a specific property depends on whether it is a single-value (defined *functional* or with maxCardinality = 1) or a multi-value (all others) property. Some remarks: 

*   Replace *Property* by the name of the property. For a property named *amount*, the methods are called *getAmount*, *setAmount*, etc. 
*   For object properties, *Range* refers to the class of the property while *IRange* refers to the interface that defines it;  
    for datatype properties both *Range* and *IRange* are simply an appropriate Java primitive type. 

##### Single-value properties 

    public Range getProperty()
    public void setProperty(IRange value)
    public boolean existsProperty()
    public boolean hasProperty(IRange value)
    public void removeProperty()
    

##### Multi-value properties 

    public List<Range> listProperty()
    public Iterator<Range> iterateProperty()
    public boolean existsProperty();
    public int countProperty()
    public void addProperty(IRange value)
    public void addAllProperty(List <? extends IRange> values)
    public void removeProperty(IRange value)
    public void removeAllProperty()
    

## Known issues & todo 

### Owl features 

*   Add support for complex range definitions (so running PropertyRangeSimplifier is no longer necessary)
*   Support property characteristics like maxCardinality, transitive, inverse functional
*   Add support for primitive types like float, int, etc. (how about lists, iterators then?)
*   Add support for XsdDuration
*   XsdTypes entity is currently ignored

### Code generation 

*   Output path could be either absolute or relative to project root
*   When writing code to somewhere outside the owl2java folder, also copy owl2java classes that the generated code depends on
*   Use Sparql for generated count() methods

### Other 

*   Add JavaDoc comments
*   More examples and general documentation
*   Make the code generator use generics instead of casts internally

## Authors and license 

The original Owl2Java, of which this project is a fork, was written by Michael Zimmermann. He should be credited for most the work. More information can be found at <http://www.incunabulum.de/projects/it/owl2java>.  
Modifications were made by Bert Belder at the TU Delft (the Netherlands), faculty of Systems Engineering, Policy Analysis and Management, Energy and Industry section. 

Both the original Owl2Java and this fork are licensed under the GNU General Public License version 2;  
see license.txt for more information.