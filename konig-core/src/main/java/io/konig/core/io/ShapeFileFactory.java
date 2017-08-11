package io.konig.core.io;

import java.io.File;

import io.konig.shacl.Shape;

public interface ShapeFileFactory {

	File createFile(Shape shape);
}
