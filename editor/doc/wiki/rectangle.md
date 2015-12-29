# Rectangle

The `Rectangle` class represents a rectangle as an array of `Segment` instances.

Given a `Rectangle` and some point `a`, you can find the point closest to the rectangle like this:

```
   var p = rect.pointClosestTo(a);
```   

The return value is a `Point` that has been decorated with an index for the side. You can get the `Segment` representing the side that contains `p` like this:

```
	var segment = rect.side[p.side];
```

`Konig.geometry` contains constants for each side:

```
	Konig.geometry.TOP = 0
	Konig.geometry.RIGHT = 1
	Konig.geometry.BOTTOM = 2
	Konig.geometry.LEFT = 3
```

One of the nice features of this definition, is that you can find the opposite side using modulo arithmetic, like this.

```
	var oppositeSide = rect.side[(p.side+2)%4];
```


		