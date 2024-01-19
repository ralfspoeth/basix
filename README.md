# basix - Basic Data Structures and Functions

This tiny library contains some structures and functions
which I frequently use by myself - and offer therefore for 
others as well.

## Getting Started

Use these maven coordinates to incorporate the library in your
work:

    groupId: io.github.ralfspoeth
    artefactId: basix
    version: 1.0.8

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

## Indexed and Labeled

The `indexed` methods are used to attach an index to an element in a stream like so:

    Stream.of("One", "Two", "Three")
        .map(indexed(1)) // add an index starting with 1
        .toList(); 
    // [Indexed(1, "One"), Indexed(2, "Two"), Indexed(3, "Three")]


The `labeled` methods turns a map of key-value pairs into a stream of key-labeled values:

    var m = Map.of("A", "AAA", "B", "BBB");
    Functions.labeled(m); 
    // equivalent Stream.of(new Labeled("A", "AAA"), new Labeled("B", "BBB"));


### Where does `indexed` help us?

While the `.stream()` method preserves the order of an ordered collection
when in the sequential mode, we loose the index of an object in - say - an 
`ArrayList`. We may nevertheless need that index; consider the `PreparedStatement` in the 
JDBC package for example:

    Connection conn; // some connection
    var args = List.of("1", "One", "Two");
    var ps = conn.prepareStatement("insert into A values(?, ?, ?)");
    
We may then either code

    for(int i = 0; i<args.size(); i++) {
        ps.setString(i+1, args.get(i));
    }

or else

    args.map(indexed(1)).forEach(ia -> // ia == indexedArg
            ps.setString(ia.index(), ia.value()));

or

    indexed(args, 1).forEach(
            ps.setString(ia.index(), ia.value()));

ignoring the `SQLException`s for brevity.

### How can we utilize `labeled`?

The most useful application of `labeled` is when we're actually interested in the values
of a map but don't want to lose the key, or when you want to make a property of some complex
object the label (or key) of that object in a list _without_ the requirement that this label be unique.

    var m = Map.of("One", 1, "Two", 2, "Three", 3);
    labeled(m).map(item -> item.mod(k -> k*k)).toList(); 
    // ["One", 1, "Two", 4, "Three", 9]

Or, let

    record Comp(String name, int age){}
    var compList = List.of(new Comp("A", 5), new Comp("B", 7)); 

then

    var cl = compList.map(labeled(Comp::name)).toList();
    // [Labeled("A", Comp(name="A", age=5)), Labeled("B", Comp(name="B", age=7))]

We may then mess with the label as in

    var nl = compList.map(labeled(Comp::name))
        .map(lv -> lv.modValue(lv -> lv.value * lv.value))
        .toList();
    // same result as above

or with the key as in

    var kl = compList.map(labeled(Comp::age))
        .map(lv -> lv.modKey(k -> k-10))
        .toList();
    // [Labeled(-5, Comp(name="A", age=5)), 
        Labeled(-3, Comp(name="B", age=7))]        

...nothing of which cannot be done with `Map.Entry` for sure, yet the notion
of labeling objects is quite common and is therefore semantically useful while
`Map.Entry::key` refers to an identifying unique tag rather than - well - just a label.

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

How does that help? Consider this:

    class ComplexObject {
        OtherBig ob, oc;
        VeryLarge vl;
        Map<String, String> alternatives = new HashMap<>();
        String name;
    }

    var l = List.of(...); // ComplexObject instances

We may then want to print the `vl` contents of all instances in `l` where the
name starts with `'X'`; now we can write

    import static io.github.ralfspoeth.basix.fn.Predicates.*;
    // ...
    l.stream()
        .filter(eq('X', co -> co.name.charAt(0))) 
        .map(co -> co.vl)
        .forEach(System.out::println);

or, where the name matches `'X'` or `'Y'`

    l.stream()
        .filter(in(Set.of('X', 'Y'), co -> co.name.charAt(0))) 
        ...
    // equivalent to 
        .filter(co -> Set.of('X', 'Y').contains(co.name.charAt(0)))

or like

    l.stream()
        .filter(in(Map.of('X', 1, 'Y', 2), co -> co.name.charAt(0)))
        ...
    // equivalent to 
        .filter(co -> Map.of('X', 1, 'Y', 2).containsKey(co.name.charAt(0)))
        ...

There seems to be little gain here, but consider this function which returns
alternative names for a complex object:

    Function<ComplexObject, String> altName(String altNameType) {
        return co -> co.alternatives.get(altNameType);
    }

We can then write

    l.stream()
        .filter(in(Set.of("Aaa", "Bbb"), altName("nickname"))
        ...

which is semantically clearer than

    l.stream()
        .filter(co -> Set.of("Aaa", "Bbb")
            .contains(co.alternatives.get("nickname")
        )
        ...

and considering that the extraction function may be defined once and then reused:

    Function<ComplexObject, String> nickname = altName("nickname");
    var nickies = Set.of("Aaa", "Bbb");

    l.stream().filter(in(nickies, nickname))...;

we may have more readable code in the end.
But I admit it's a matter of taste...
