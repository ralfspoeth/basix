# basix - Basic Data Structures and Functions

This tiny library contains some structures and functions
which I frequently use by myself - and offer therefore for 
others as well.

## Getting Started

Use these maven coordinates to incorporate the library in your
work:

    groupId: io.github.ralfspoeth
    artefactId: basix
    version: 1.0.7

You'll need Java version 21 or later to utilize this library.

When working with JPMS modules, add this to your
`module-info.java` file:

    module your.module {
        requires io.github.ralfspoeth.basix;
    }

# Purity Stack and Queue Implementations

Though the Java Collections library contains a rich set
of classes which provide much more functionality then 
these two `Stack` and `Queue` implementations I prefer
them frequently when I want to utilize their push/pop or 
add/remove semantics, respectively, in pure manner.

## Stack

The LIFO (last in, first out) stack structure, defined as an abstract data type,
provides, the two operations `push` and `pop` and the two 
state enquiries `isEmpty` and `top` only.

Hence

    var s = new Stack<T>();
    assert s.isEmpty();
    assert s.top()==null;

and 

    s.push(t);
    assert t == s.pop();

as well as

    s.push(t);
    s.push(u);
    assert u == s.pop() && t == s.pop() && s.isEmpty();

## Queue

The FIFO (first in, first out) queue is similar but provides
only `add` (or `addLast`) and `remove` (or `removeFirst`) 
operations and -- as the stack -- `isEmpty` and additionally
`head` and `tail` operations. The `head` of a queue is the 
least recent element added to the queue and `tail` is the most recent 
element.

Hence,

    var q = new Queue<T>();
    assert q.isEmpty() && q.head()==null && q.tail()==null;

and for the operations

    q.add(t);
    q.add(u);
    assert t == q.remove() &&  u == q.remove() && q.isEmpty();

Both queues and stacks do not implement either of the 
interfaces of the Java Collections Framework. They are useful 
exclusively when Java collections simply provide way to much
for the task at hand.

# Functions and Predicates

The two classes `Functions` and `Predicates` provide utilities
to instantiate functional interface implementations based on `Map`s and `List`s
plus support for adding indexed and labelled data, and
predicates (function with a `boolean` return value) based on `Map`s and `Set`s.

## Functions

The factory method `Functions::of(Map, Function)` returns a function which 
first extracts some property of a given object and then looks up the value
for this property in the given map.

Let

    record Point(int x, int y) {}

and
    
    var m = Map.of(1, "One", 2, "Two");

then

    Function<Point, String> f = Functions.of(m, Point::x);
    assert f.apply(new Point(1, 10)).equals("One");

where

    Function<Point, Integer> extr = Point::x;

is the extraction function.
Note that a map can be easily turned into a function using the method reference `Map::get`
so there is no companion for this.

Similarly, `Functions::of(List)` returns an `IntFunction` as in

    var primes = List.of(2, 3, 5, 7, 11);
    IntFunction<Integer> f = Functions.of(primes);
    assert 3 == f.apply(1);

Implementation note: both factory methods make a defensive copy of the given list or map and are
therefore unmodifiable; and immutable when the elements of the list or map are immutable.

The `indexed` methods are used to attach an index to an element in a stream like so:

    Stream.of("One", "Two", "Three")
        .map(indexed(1))
        .toList(); 
    // [Indexed(1, "One"), Indexed(2, "Two"), Indexed(3, "Three")]

The `labeled` methods turns a map of key-value pairs into a stream of key-labeled values:

    var m = Map.of("A", "AAA", "B", "BBB");
    Functions.labeled(m); // equivalent Stream.of(new Labeled("A", "AAA"), new Labeled("B", "BBB"));

## Predicates

The `Predicates::in` factory methods instantiate predicates which take a map or a set and an 
extraction function such that

    var s = Set.of(1, 2, 3);
    record Point(int x, int y) {}
    Predicate<Point> pp = Predicates.in(s, Point::y);
    // equivalently pp = p->s.contains(p.y());

or for maps

    var m = Map.of(1, "A", 2, "B", 3, "C");
    record Point(int x, int y) {}
    Predicate<Point> pp = Predicates.in(s, Point::y);
    // equivalently pp = p -> s.containsKey(p.y());

Similarly, the `Predicates::eq` factory method creates a predicate the checks for equality
using `Objects::equals` internally:

    record Point(int x, int y){}
    Predicate<Point> y3 = Predicates.eq(3, Point::y);
    // equivalently y3 = p -> Objects.equals(3, p.y());

