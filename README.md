# FactorioManufacturingPlanner
This program intends to make planning modular factories easier. When you decide to manufacture a number of items and have a set items as inputs it will calculate the quantity of those inputs required, the number of belt lanes they will occupy and the number of manufacturing machines you'd need for any intermediaries.

## Installation
At the moment I haven't provided any pre-built installers or <code>.jar</code> files, the p[roject is being developed in e(fx)clipse and should be importable into an existing e(fx)clipse workspace as it, and run in the ide with no modifications.

## Usage

<ol>
<li>File -> load recipes</li>
<li>Select the <b>Factory Settings</b> section</li>
<li>Select one or more items to produce and click on the top <code>&gt;</code> button to add them as a product of your factory</li>
<li>Modify the quantity and/or units of the item to be produced by selecting the Outpiuts table and double clicking on a value to change, hit enter to ensure the modification is saved.</li>
<li>Optionally select one or more items that will be pre-prepared and supplied to the factory as inputs (note that raw inputs will be calculated and do not need to be added here, these optional inputs are for intermediate products which have already been produced elsewhere in your factory) and hit the bottom <code>&gt;</code> button.</li>
<li>The <code>&lt;</code> buttons remove selected products or optional inputs from their list or table.</li>
<li>Select the <b>Factory Details</b> section</li>
<li>Here the steps requitred to manufacture your desired outputs, at the desired rate are listed and a consolidated list of all of the inputs required to do so, and the quantities required</li>
</ol>

## Notes
The program currently defaults to the first recipe it finds for any particular item, so if there are multiple ways to produce an item, there is currently no way to select an alternative recipe

If the program cannot find a recipe which produces an item, it will assume the item is a raw material and require it as a factory input, this include iron and copper plates.

## TODO
<li><strike>manually convert game files (.lua) to JSON and populate logic</strike></li>
<li><strike>Allow the user to set what & how much the factory should produce and display the steps required to achieve that</strike></li> 
<li><strike>Show a consolidated list of the factory inputs required</strike></li>
<li><strike>Allow the user to select items which will be pre-prepared and remove them from the list of steps required, and add them to the required inputs</strike></li>
<li>Convert game files to JSON in code</li>
<li>Allow a Factorio installation directory to be set
<ul>
<li>Get all files containing recipes from the base game</li>
<li>Get all files containing recipes from mods</li>
<li>Get all files containing item definitions from base game</li>
<li>Get all files containing item definitions from mods</li>
</ul></li>
<li>Locate icons within the game files and include them in the tables and lists</li>
<li>Show items/second as the number of lanes of conveyor belt required and allow the setting of which colour belt to use (default to something sensible)</li>
<li>Allow alternative recipes to be used for items which could be produced multiple ways</li>
<li>Detectif item is a fluid and display maximum pipe length (without a pump) for required throughput</li>
<li>Allow user to set if using expensive recipe mode</li>
<li>Get alternate line shading working in TreeTable to make viewing the factory easier</li>
<li>Round doubles to 2 decimal places</li>
<li>Show how many manufacturing machines would be required to produce at the required rate, again default to a sensible manufacturer tier but allow user to select any <i>valid</i> tier</li>
<li>Allow user to specify if modules are used in manufacturers on a per recipe basis</li>
<li>Allow user to specify number of beacons per manufacturer and what modules they contain</li>
