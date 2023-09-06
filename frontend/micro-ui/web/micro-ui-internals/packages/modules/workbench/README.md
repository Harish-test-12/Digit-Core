<!-- TODO: update this -->

# digit-ui-module-workbench

## Install

```bash
npm install --save digit-ui-module-workbench
```

## Limitation

```bash
This Package is more specific to DIGIT-UI's can be used across mission's
```

## Usage

After adding the dependency make sure you have this dependency in

```bash
frontend/micro-ui/web/package.json
```

```json
"@egovernments/digit-ui-module-workbench":"0.0.1",
```

then navigate to App.js

```bash
 frontend/micro-ui/web/src/App.js
```

```jsx
/** add this import **/

import { initWorkbenchComponents } from "@egovernments/digit-ui-module-workbench";

/** inside enabledModules add this new module key **/

const enabledModules = ["workbench"];

/** inside init Function call this function **/

const initDigitUI = () => {
  initWorkbenchComponents();
};

```

In MDMS

_Add this configuration to enable this module [MDMS Enabling Workbench Module](https://github.com/egovernments/works-mdms-data/blob/588d241ba3a9ab30f4d4c2c387a513da811620ca/data/pg/tenant/citymodule.json#L227)_

## List of Screens available in this versions were as follows

1. Search Master Data
    // TODO Add more information


2. Add Master Data based on selected schema
    // TODO Add more information


3. Update Master data for selected data.
    // TODO Add more information

# Mandatory changes to use Workbench module

1. Assuming core module is already updated with 1.5.38+ and related changes were taken

2. add the following hook method in micro-ui-internals/packages/libraries/src/hooks/useCustomAPIMutationHook.js

reference:: 
https://github.com/egovernments/DIGIT-Dev/blob/6e711bdc005c226c7debd533209681fc77078a3e/frontend/micro-ui/web/micro-ui-internals/packages/libraries/src/hooks/useCustomAPIMutationHook.js

3. add the following utility method in micro-ui-internals/packages/libraries/src/utils/index.js
```jsx
didEmployeeHasAtleastOneRole

const didEmployeeHasAtleastOneRole = (roles = []) => {
  return roles.some((role) => didEmployeeHasRole(role));
};

```

4. stylesheet link has to be added 
```jsx
<link rel="stylesheet" href="https://unpkg.com/@egovernments/digit-ui-css@1.2.114/dist/index.css" />
```
Reference commit for the enabling workbench
https://github.com/egovernments/DIGIT-OSS/pull/99/commits/6e711bdc005c226c7debd533209681fc77078a3e

## Coming Soon

1. Localisation screens
2. MDMS UI Schema
3. Data push for any API based on schema


# Changelog

```bash
0.0.3 readme updated
0.0.2 readme updated
0.0.1 base version
```

# Contributors

[jagankumar-egov] 

## Published from DIGIT Core 
Digit Dev Repo (https://github.com/egovernments/Digit-Core/tree/digit-ui-core)

## License

MIT © [jagankumar-egov](https://github.com/jagankumar-egov)