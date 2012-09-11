describe("Suite from file 2", function() {
	it("exists to test multi file support in PhantomRunner", function() {
		expect(true).toBe(true);
	});
	
	it("and also it may fail too", function() {
		expect(true).not.toBe(true);
	});
});