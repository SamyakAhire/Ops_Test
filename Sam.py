# sample.py

def greet(name):
    return f"Hello, {name}! Welcome to the project."


def add_numbers(a, b):
    return a + b


def sub_numbers(a, b):
    return a - b

def mul_numbers(a, b):
    return a * b


if __name__ == "__main__":
    name = "Samarth"

    print(greet(name))
    print("10 + 20 =", add_numbers(10, 20))
    print("10 - 20 =", sub_numbers(40, 20))
    print("10 - 20 =", mul_numbers(2, 2))