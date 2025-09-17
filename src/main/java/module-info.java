import org.jspecify.annotations.NullMarked;

@NullMarked
module io.github.ralfspoeth.basix {
    requires org.jspecify;
    exports io.github.ralfspoeth.basix.coll;
    exports io.github.ralfspoeth.basix.fn;
}