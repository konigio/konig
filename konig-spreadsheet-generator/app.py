import pandas as pd
import argparse

from rdflib import Graph, URIRef, Literal, Namespace, RDF, RDFS, OWL, XSD

class Ontology:

    ONTOLOGY_NAME = "Ontology Name"
    COMMENT = "Comment"
    NAMESPACE_URI = "Namespace URI"
    PREFIX = "Prefix"

    def __init__(self):
        self.name = None
        self.comment = None
        self.uri = None
        self.prefix = None

    def to_dict(self):
        return {
            Ontology.ONTOLOGY_NAME: self.name,
            Ontology.COMMENT: self.comment,
            Ontology.NAMESPACE_URI: self.uri,
            Ontology.PREFIX: self.prefix
        }

    @staticmethod
    def columns():
        return [Ontology.ONTOLOGY_NAME,
                Ontology.COMMENT,
                Ontology.NAMESPACE_URI,
                Ontology.PREFIX]


class OWLClass:

    NAME = "Class Name"
    COMMENT = "Comment"
    ID = "Class Id"
    SUBCLASS_OF = "Subclass Of"

    def __init__(self):
        self.name = None
        self.comment = None
        self.id = None
        self.subclass_of = None

    def to_dict(self):
        return {
            OWLClass.NAME: self.name,
            OWLClass.COMMENT: self.comment,
            OWLClass.ID: self.id,
            OWLClass.SUBCLASS_OF: self.subclass_of
        }

    @staticmethod
    def factory(g, c):
        _class = OWLClass()
        _class.name = str(next(g.objects(c, RDFS.label), None))
        _class.comment = next(g.objects(c, RDFS.comment), None)
        _class.id = apply_prefix(c)
        _class.subclass_of = "\n".join([apply_prefix(_parent) for _parent in g.objects(c, RDFS.subClassOf)])
        return _class

    @staticmethod
    def columns():
        return [OWLClass.NAME,
                OWLClass.COMMENT,
                OWLClass.ID,
                OWLClass.SUBCLASS_OF]


class OWLProperty:

    NAME = "Property Name"
    COMMENT = "Comment"
    ID = "Property Id"
    DOMAIN = "Domain"
    RANGE = "Range"
    INVERSE_OF = "Inverse Of"
    TYPE = "Property Type"

    def __init__(self):
        self.name = None
        self.comment = None
        self.id = None
        self.domain = None
        self.range = None
        self.inverse_of = None
        self.type = None

    def to_dict(self):
        return {
            OWLProperty.NAME: self.name,
            OWLProperty.COMMENT: self.comment,
            OWLProperty.ID: self.id,
            OWLProperty.DOMAIN: self.domain,
            OWLProperty.RANGE: self.range,
            OWLProperty.INVERSE_OF: self.inverse_of,
            OWLProperty.TYPE: self.type
        }

    @staticmethod
    def factory(g, p):
        _property = OWLProperty()
        _property.name = str(next(g.objects(p, RDFS.label), None))
        _property.comment = next(g.objects(p, RDFS.comment), None)
        _property.id = apply_prefix(p)
        _property.range = apply_prefix(next(g.objects(p, RDFS.range), None))
        _property.domain = apply_prefix(next(g.objects(p, RDFS.domain), None))
        _property.inverse_of = apply_prefix(next(g.objects(p, OWL.inverseOf), None))
        _property.type = apply_prefix(next(g.objects(p, RDF.type), None))
        return _property

    @staticmethod
    def columns():
        return [OWLProperty.NAME,
                OWLProperty.COMMENT,
                OWLProperty.ID,
                OWLProperty.DOMAIN,
                OWLProperty.RANGE,
                OWLProperty.INVERSE_OF,
                OWLProperty.TYPE]


class Shape:

    ID = "Shape Id"
    COMMENT = "Comment"
    TARGET_CLASS = "Target Class"
    DATASOURCE = "Datasource"
    IRI_TEMPLATE = "IRI Template"
    MEDIA_TYPE = "Media Type"

    def __init__(self):
        self.id = None
        self.comment = None
        self.target_class = None
        self.datasource = None
        self.iri_template = None
        self.media_type = None

    def to_dict(self):
        return {
            Shape.ID: self.id,
            Shape.COMMENT: self.comment,
            Shape.TARGET_CLASS: self.target_class,
            Shape.DATASOURCE: self.datasource,
            Shape.IRI_TEMPLATE: self.iri_template,
            Shape.MEDIA_TYPE: self.media_type
        }

    @staticmethod
    def factory(g, s):
        _shape = Shape()
        _shape.id = apply_prefix(s)
        _shape.comment = next(g.objects(s, RDFS.comment), None)
        _shape.target_class = apply_prefix(next(g.objects(s, SH.targetClass), None))
        return _shape

    @staticmethod
    def columns():
        return [Shape.ID,
                Shape.COMMENT,
                Shape.TARGET_CLASS,
                Shape.DATASOURCE,
                Shape.IRI_TEMPLATE,
                Shape.MEDIA_TYPE]


class PropertyConstraint:

    SHAPE_ID = "Shape Id"
    PROPERTY_ID = "Property Id"
    COMMENT = "Comment"
    VALUE_TYPE = "Value Type"
    MIN_COUNT = "Min Count"
    MAX_COUNT = "Max Count"
    VALUE_CLASS = "Value Class"
    STEREOTYPE = "Stereotype"
    FORMULA = "Formula"
    VALUE_IN = "Value In"
    MIN_INCLUSIVE = "Min Inclusive"
    MAX_EXCLUSIVE = "Max Exclusive"
    EQUIVALENT_PATH = "Equivalent Path"
    HAS_VALUE = "Has Value"

    def __init__(self):
        self.shape_id = None
        self.property_id = None
        self.comment = None
        self.value_type = None
        self.min_count = None
        self.max_count = None
        self.value_class = None
        self.stereotype = None
        self.formula = None
        self.value_in = None
        self.min_inclusive = None
        self.max_exclusive = None
        self.equivalent_path = None
        self.has_value = None  # ???

    def to_dict(self):
        return {
            PropertyConstraint.SHAPE_ID: self.shape_id,
            PropertyConstraint.PROPERTY_ID: self.property_id,
            PropertyConstraint.COMMENT: self.comment,
            PropertyConstraint.VALUE_TYPE: self.value_type,
            PropertyConstraint.MIN_COUNT: self.min_count,
            PropertyConstraint.MAX_COUNT: self.max_count,
            PropertyConstraint.VALUE_CLASS: self.value_class,
            PropertyConstraint.STEREOTYPE: self.stereotype,
            PropertyConstraint.FORMULA: self.formula,
            PropertyConstraint.VALUE_IN: self.value_in,
            PropertyConstraint.MIN_INCLUSIVE: self.min_inclusive,
            PropertyConstraint.MAX_EXCLUSIVE: self.max_exclusive,
            PropertyConstraint.EQUIVALENT_PATH: self.equivalent_path,
            PropertyConstraint.HAS_VALUE: self.has_value
        }

    @staticmethod
    def factory(g, p):
        _constraint = PropertyConstraint()
        _constraint.shape_id = apply_prefix(next(g.subjects(SH.property, p), None))
        _constraint.property_id = apply_prefix(str(next(g.objects(p, SH.path), None)))

        _max = next(g.objects(p, SH.maxCount), None)
        _constraint.max_count = _max.toPython() if _max else None

        _min = next(g.objects(p, SH.minCount), None)
        _constraint.min_count = _min.toPython() if _min else 0

        _constraint.value_class = apply_prefix(next(g.objects(p, SH["class"]), None))

        _node_kind = next(g.objects(p, SH.nodeKind), None)
        _datatype = next(g.objects(p, SH.datatype), None)
        _node = next(g.objects(p, SH.node), None)

        if _node_kind == SH.IRI and not _node:
            _constraint.value_type = apply_prefix(XSD.anyURI)
        elif _node_kind == SH.IRI and _node:
            _constraint.value_type = apply_prefix(_node)
        else:
            _constraint.value_type = apply_prefix(_datatype)

        return _constraint

    @staticmethod
    def columns():
        return [PropertyConstraint.SHAPE_ID,
                PropertyConstraint.PROPERTY_ID,
                PropertyConstraint.COMMENT,
                PropertyConstraint.VALUE_TYPE,
                PropertyConstraint.MIN_COUNT,
                PropertyConstraint.MAX_COUNT,
                PropertyConstraint.VALUE_CLASS,
                PropertyConstraint.STEREOTYPE,
                PropertyConstraint.FORMULA,
                PropertyConstraint.VALUE_IN,
                PropertyConstraint.MIN_INCLUSIVE,
                PropertyConstraint.MAX_EXCLUSIVE,
                PropertyConstraint.EQUIVALENT_PATH,
                PropertyConstraint.HAS_VALUE]


SCHEMA = Namespace("http://schema.org/")
SKOS = Namespace("http://www.w3.org/2004/02/skos/core#")
KONIG = Namespace("http://www.konig.io/ns/core/")
ORG = Namespace("http://www.w3.org/ns/org#")
MDM = Namespace("https://schema.pearson.com/ns/mdm/")
REG = Namespace("https://schema.pearson.com/ns/registrar/")
CNT = Namespace("https://schema.pearson.com/ns/content/")
SH = Namespace("http://www.w3.org/ns/shacl#")
SHAPE = Namespace("https://schema.pearson.com/shapes/")

g = Graph()

g.bind("schema", SCHEMA)
g.bind("reg", REG)
g.bind("sh", SH)
g.bind("konig", KONIG)
g.bind("skos", SKOS)
g.bind("cnt", CNT)
g.bind("mdm", MDM)
g.bind("shape", SHAPE)
g.bind("owl", OWL)
g.bind("org", ORG)


def get_namespace_hash():
    _hash = {}
    for prefix, ns in g.namespace_manager.namespaces():
        _hash.update({str(ns): prefix})
    return _hash


r_namespaces = get_namespace_hash()
default_prefix = "ex"


def apply_prefix(uri):

    if not uri:
        return uri

    for k, v in r_namespaces.items():
        if uri.startswith(k):
            if v:
                return uri.replace(k, v + ":")
            else:
                return uri.replace(k, default_prefix+":")
    return uri


def to_dictlist(collection):
    return [item.to_dict() for item in collection]


def get_ontologies(g):
    ontologies = list()
    for prefix, ns in g.namespace_manager.namespaces():
        _ontology = Ontology()
        _ontology.uri = str(ns)
        _ontology.prefix = prefix
        ontologies.append(_ontology)
    return ontologies


def write_xslx(outfile, data):

    writer = pd.ExcelWriter(outfile)

    if "ontologies" in data and data["ontologies"]:
        ontologies = data["ontologies"]
        df = pd.DataFrame.from_dict(to_dictlist(ontologies))
        df.to_excel(writer, 'Ontologies', index=False, columns=Ontology.columns())

    if "classes" in data and data["classes"]:
        classes = data["classes"]
        df = pd.DataFrame.from_dict(to_dictlist(classes))
        df.to_excel(writer, 'Classes', index=False, columns=OWLClass.columns())

    if "properties" in data and data["properties"]:
        properties = data["properties"]
        df = pd.DataFrame.from_dict(to_dictlist(properties))
        df.to_excel(writer, 'Properties', index=False, columns=OWLProperty.columns())

    if "shapes" in data and data["shapes"]:
        shapes = data["shapes"]
        df = pd.DataFrame.from_dict(to_dictlist(shapes))
        df.to_excel(writer, 'Shapes', index=False, columns=Shape.columns())

    if "constraints" in data and data["constraints"]:
        property_constraints = data["constraints"]
        df = pd.DataFrame.from_dict(to_dictlist(property_constraints))
        df.to_excel(writer, 'Property Constraints', index=False, columns=PropertyConstraint.columns())

    writer.save()


def main(infile, outfile, format="turtle"):

    g.parse(infile, format=format)

    data = {}

    data.update({"ontologies": get_ontologies(g)})
    data.update({"classes": [OWLClass.factory(g, c) for c in g.subjects(RDF.type, OWL.Class)]})

    properties = [OWLProperty.factory(g, p) for p in g.subjects(RDF.type, OWL.ObjectProperty)]
    properties += [OWLProperty.factory(g, p) for p in g.subjects(RDF.type, OWL.DatatypeProperty)]
    properties += [OWLProperty.factory(g, p) for p in g.subjects(RDF.type, OWL.AnnotationProperty)]
    data.update({"properties": properties})

    data.update({"shapes": [Shape.factory(g, s) for s in g.subjects(RDF.type, SH.NodeShape)]})
    data.update({"constraints": [PropertyConstraint.factory(g, p) for p in g.objects(None, SH.property)]})

    write_xslx(outfile, data=data)


if __name__ == "__main__":

    parser = argparse.ArgumentParser(description="Create spreadsheet from RDF/OWL")
    parser.add_argument('input', help="input OWL/RDF file")
    parser.add_argument('output', help="output XLSX file")
    parser.add_argument('--format', default="turtle", help="RDF format")

    args = parser.parse_args()

    main(args.input, args.output, args.format)
