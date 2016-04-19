
# Database assertions

JDBDT assertions are of a special kind, called **delta assertions**. 
The idea is that programmers may state the expected incremental changes made to the database,
i.e., the expected database delta. The figure below illustrates the mechanism. 
Starting from state **S**, called the **reference snapshot**, the SUT acts on a database, 
yielding a new database state, **S'**. **S** and **S'** will have in common **unchanged** data **U**,
but will differ by delta **(O,N)**, where **O = S - S'** is **old** data in **S** no longer defined in **S'**, 
and **N = S' - S** is **new** data in **S'**.

![Database delta](images/jdbdt-delta.png)

## Snapshot definition 

## Assertion methods 
