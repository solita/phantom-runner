require.config({
    baseUrl: "",
    waitSeconds: 5
});

require(["require-test-func"], function(requiretestfunc) {
    
    describe("Functionality from external-test-func.js using RequireJS", function() {
        it("sees exposed functions", function() {
            expect(requiretestfunc.hello("Tester")).toBe("Hello Tester!");
        });
    });
    
});