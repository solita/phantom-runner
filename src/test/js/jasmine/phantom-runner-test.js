describe("A suite", function() {
    it("contains a specification with an expectation", function() {
        expect(true).toBe(true);
    });
});

describe("Another suite", function() {
    it("will do other checks", function() {
        expect("foo").toBe("foo");
    });
    
    it("and some specifications which may fail", function() {
        expect("foo").toBe("bar");
    })
});