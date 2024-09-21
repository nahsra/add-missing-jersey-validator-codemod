This change adds `@Valid` annotations to Jersey POJO inputs that don't have them.

After adding the missing annotation, we may also add "common sense" validations to the DTO based on the context.  