# Konig Transform Beam Design Notes

## PMethods

Each `sh:PropertyShape` within the target Node Shape has a method that computes and captures
the value of that property.  We call such a method a `PMethod`.

A given PMethod may contain the following kinds of parameters:

1. TableRow for source records used to compute the value
2. TableRow for the target record that will hold the value
3. Enum member(s) with one or more fields referenced by the value's expression
4. An ErrorBuilder

The strategy for determining the parameter set of a PMethod depends on whether the property 
value is a nested record.  If the property value is a nested record, we say that the property
is _complex_.  Otherwise, we say that the property is _simple_.

We discuss the strategy for computing the parameter set in both cases below.

### Parameter Set for Simple Properties

For each (source or target) property referenced by the value expression, include a parameter for the _container_ of the property value.  The container is either an enum member or a TableRow, depending on the type of value.

### Parameter Set for Complex Properties

Create a local variable for the nested record.  This value does not need to be passed to the method as a parameter.
For each field in the nested record, generate the PMethod.  

Compute the union of all parameters required by the nested fields, minus any parameters matched by the local variable.

Use this union as the parameter set.


## Handling dependencies

Sometimes a given target property depends on the value of another target property.  We  
ensure that the given property is computed after the dependency by sorting the properties based on the dependency tree.
Currently, we are sorting only within the scope of a given NodeShape.  If a dependency crosses NodeShape boundaries,
then we do not guarantee that that ordering is correct.  This is a known bug.  To fix it, we will need to move the
creation of the given target property out of the method for the parent NodeShape and handle it separately as a kind of
post-processing task.

##     