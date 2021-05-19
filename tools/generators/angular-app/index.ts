import { Tree, formatFiles, installPackagesTask, generateFiles } from '@nrwl/devkit';
import { wrapAngularDevkitSchematic } from '@nrwl/tao/src/commands/ngcli-adapter';
import { stringUtils } from '@nrwl/workspace';
import * as path from 'path';

export const libraryGenerator = wrapAngularDevkitSchematic('@nrwl/angular', 'app');

export interface SchematicOptions {
  name: string;
}

export default async function (tree: Tree, options: SchematicOptions) {
  await libraryGenerator(tree, options);
  await formatFiles(tree);
  await deleteRootEslintFile(tree);
  await replaceEslintFileInApp(tree, options);
  await replaceEslintFileInE2E(tree, options);
  return () => {
    installPackagesTask(tree);
  };
}

async function deleteRootEslintFile(tree: Tree) {
  tree.delete('.eslintrc.json');
}

async function replaceEslintFileInApp(tree: Tree, options: SchematicOptions) {
  const appPath = path.join('apps', stringUtils.dasherize(options.name));
  const filesPath = path.join(__dirname, './files/app');
  const substitutions = {
    appPath,
    relativePathFromAppToRoot: path.relative(path.dirname(appPath), path.dirname(tree.root)),
  };
  tree.delete(`${appPath}/.eslintrc.json`);
  generateFiles(tree, filesPath, appPath, substitutions);
}

async function replaceEslintFileInE2E(tree: Tree, options: SchematicOptions) {
  const e2ePath = path.join('apps', `${stringUtils.dasherize(options.name)}-e2e`);
  const filesPath = path.join(__dirname, './files/e2e');
  const substitutions = {
    e2ePath,
    relativePathFromE2EToRoot: path.relative(path.dirname(e2ePath), path.dirname(tree.root)),
  };
  tree.delete(`${e2ePath}/.eslintrc.json`);
  generateFiles(tree, filesPath, e2ePath, substitutions);
}
