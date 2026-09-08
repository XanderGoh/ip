# Kia User Guide

// Update the title above to match the actual product name

// Product screenshot goes here

// Product intro goes here

## Adding deadlines

// Describe the action and its outcome.

// Give examples of usage

Example: `keyword (optional arguments)`

// A description of the expected outcome goes here

```
expected output
```

## Feature ABC

// Feature details

## Editing tasks

Use `update` followed by the one-based task number and one or more fields.
Descriptions and event values may contain spaces. Deadline dates must use
`yyyy-MM-dd`; event boundaries remain text values. The known field markers are
reserved as update syntax when they appear as standalone tokens.

```text
update 1 /description read reference book
update 2 /by 2026-10-15
update 3 /description project meeting /from Mon 2pm /to 4pm
```

Kia confirms a successful update without changing the task's position or
completion status:

```text
Got it! I've updated this task:
  [D][ ] return book (by: Oct 15 2026)
```

The `Edit task` button in the JavaFX window opens a form for selecting a task,
changing its details, and saving the edit. The form uses the same validation
and persistence behavior as the `update` command.

Invalid task numbers, blank values, unsupported fields, incompatible fields,
duplicate fields, and invalid deadline dates are rejected without changing the
task list.


## Feature XYZ

// Feature details
