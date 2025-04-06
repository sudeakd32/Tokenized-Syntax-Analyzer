# Tokenized-Syntax-Analyzer

This project is a **basic Java Syntax Analyzer** designed to read C-style Java-like code from an input file, detect syntax errors, and output a cleaned and analyzed version of the code along with descriptive error messages.

## 🔍 What Does It Do?

- Reads code from an `input.txt` file.
- Detects and reports:
  - Missing semicolons (`;`)
  - Unmatched or missing curly braces (`{` or `}`)
  - Invalid or malformed `if` and `while` conditions
  - Invalid identifiers (only lowercase single-character names like `a`, `b`, `x`, etc. are valid)
- Splits the input into individual statements and classifies them as:
  - **Variable Declarations** → e.g., `int a;`
  - **Assignments** → e.g., `a = 3 + 2;`
  - **Return Statements** → e.g., `return x;`
  - **Conditional Statements** → `if`, `while`
- Writes the output (including corrections and error messages) to `output.txt`.


## 🧪 Statement Validation Rules

| Statement Type       | Validation Criteria                                                                 |
|----------------------|--------------------------------------------------------------------------------------|
| Variable Declaration | Must follow `type identifier;`, e.g., `int x;`                                      |
| Assignment           | Must follow `identifier = expression;`, identifiers must be single lowercase letters |
| Return Statement     | Must be `return identifier;` or `return expression;`                                 |
| If / While Condition | Must have a valid comparison like `a < b`, and be enclosed in parentheses `( )`     |


## 🚫 Error Handling

The program detects and auto-corrects common syntax issues:

- **Missing Semicolons**  
  Adds a semicolon and logs an error message.

- **Missing or Unmatched Braces**  
  Tracks curly braces using a stack and inserts missing braces when necessary.

- **Malformed Conditions**  
  Flags conditions that don’t match the expected pattern (e.g., `a + b < c * d`).

- **Invalid Identifiers**  
  Only allows lowercase letters like `a`, `b`, `x` — rejects anything else as invalid.

Each error is logged in the `output.txt` with a corresponding fix note.
  
