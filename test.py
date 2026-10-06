def divide_numbers(a, b):
    # Bug 1: ZeroDivisionError with no error handling
    return a / b

def get_user_status(age):
    # Bug 2: NameError - referencing an undefined variable
    if age >= 18:
        return adult_status
    return "minor"

def test_pipeline_failure():
    # Deliberate test failure assertion
    result = divide_numbers(10, 0)
    assert result == 5, "Assertion Error: Math calculation failed!"

if __name__ == "__main__":
    test_pipeline_failure()