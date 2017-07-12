[![wercker status](https://app.wercker.com/status/0ee80f05342e6771985269dae3174e09/s/ "wercker status")](https://app.wercker.com/project/byKey/0ee80f05342e6771985269dae3174e09)

without admin access to the bitbucket repo i cannot add a webhook config. so to trigger a build i need to manually ping wercker so wercker goes and fetches my latest changes

    curl  -H 'Content-Type: application/json' -H  'Authorization: Bearer <wercker_access_token>' -X POST -d '{"pipelineId": "5965acbd1bee390100229607", "branch":"wercker"}' https://app.wercker.com/api/v3/runs

-----

# Essence #

Essence is a program framework on which to build various kinds of 
applications as plugins to the framework. The framework provides
the following basic features:

* Update management - Finding and applying framework and plugin updates.
* Resource management - Loading and saving files. Monitoring for internal 
  and external changes.
* Workspace management - Dividing the UI workspace into work areas, 
  providing management of user settings.

### How do I get set up? ###

* TODO Summary of set up
* TODO Configuration
* TODO Dependencies
* TODO How to run tests
* TODO Deployment instructions

### Contribution guidelines ###

* TODO Writing tests
* TODO Code review
* TODO Other guidelines

### Contact Information ###

* This project is owned by Parallel Symmetry
* Contact mark@parallelsymmetry.com for information
