import org.jspecify.annotations.NullMarked;

/**
 * Basic module with (more or less) useful
 * stuff when applying functional programming styles.
 */
@NullMarked
module io.github.ralfspoeth.basix {
    requires static org.jspecify;
    exports io.github.ralfspoeth.basix.coll;
    exports io.github.ralfspoeth.basix.fn;
}