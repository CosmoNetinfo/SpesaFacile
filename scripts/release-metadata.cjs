const fs = require('node:fs');
const source = fs.readFileSync('app/build.gradle.kts', 'utf8');
const version = source.match(/versionName\s*=\s*"([^"]+)"/)?.[1];
const code = Number(source.match(/versionCode\s*=\s*(\d+)/)?.[1]);
const tag = process.env.GITHUB_REF_NAME;
if (!version || !Number.isInteger(code) || code < 1 || tag !== `v${version}` || !/^v\d+\.\d+\.\d+$/.test(tag)) {
  throw new Error('The release tag must equal v + versionName, with a positive versionCode.');
}
console.log(`Preparing ${tag} (versionCode ${code}).`);
