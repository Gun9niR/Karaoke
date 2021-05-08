function hyphenate(str) {
  let hyphenateRE = /\B([A-Z])/g;
  return str.replace(hyphenateRE, '_$1').toLowerCase();
}

function getFileType(file) {
  const splitFilename = file.name.split('.');
  return splitFilename[splitFilename.length - 1];
}

export { hyphenate, getFileType };