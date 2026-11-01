import sharp from 'sharp';
import { mkdir, copyFile } from 'node:fs/promises';

const sizes = [48, 72, 96, 128, 144, 152, 192, 384, 512, 1024];
const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 512 512"><defs><linearGradient id="bg" x1="0" y1="0" x2="1" y2="1"><stop stop-color="#ff4d6d"/><stop offset="1" stop-color="#ff8a5c"/></linearGradient><linearGradient id="icing" x2="0" y2="1"><stop stop-color="#fffaf5"/><stop offset="1" stop-color="#ffe9dc"/></linearGradient></defs><rect width="512" height="512" rx="116" fill="url(#bg)"/><circle cx="392" cy="116" r="48" fill="#ffffff" opacity=".12"/><circle cx="112" cy="394" r="70" fill="#ffffff" opacity=".1"/><ellipse cx="256" cy="405" rx="158" ry="25" fill="#7e203c" opacity=".18"/><path d="M116 312q0-28 32-41l108-43 108 43q32 13 32 41v91H116z" fill="url(#icing)"/><path d="M116 312q31 26 63 0t64 0t64 0t63 0t26 0v50q-31 26-63 0t-64 0t-64 0t-63 0t-26 0z" fill="#ffbd9e"/><path d="M153 264q0-32 39-46l64-23 64 23q39 14 39 46v30H153z" fill="#fffaf5"/><path d="M153 264q24 20 47 0t47 0t47 0t47 0t38 0v34q-24 20-47 0t-47 0t-47 0t-47 0t-38 0z" fill="#ffd2b8"/><rect x="245" y="121" width="22" height="89" rx="11" fill="#fff8ef"/><path d="M256 76c-32 37-26 60 0 68 26-8 32-31 0-68z" fill="#fff1a3"/><path d="M256 92c-13 16-10 27 0 31 10-4 13-15 0-31z" fill="#fffcdf"/><circle cx="196" cy="281" r="5" fill="#ff906e"/><circle cx="316" cy="281" r="5" fill="#ff906e"/></svg>`;
const bytes = Buffer.from(svg);
await mkdir('web/icons', { recursive: true });
await mkdir('icons', { recursive: true });
for (const size of sizes) {
  const output = `web/icons/icon-${size}.png`;
  await sharp(bytes).resize(size, size).png().toFile(output);
  await copyFile(output, `icons/icon-${size}.png`);
}
await sharp(bytes).resize(512, 512).png().toFile('web/icons/icon-maskable-512.png');
await copyFile('web/icons/icon-maskable-512.png', 'icons/icon-maskable-512.png');

const density = [
  ['mdpi', 48], ['hdpi', 72], ['xhdpi', 96], ['xxhdpi', 144], ['xxxhdpi', 192],
];
for (const [folder, size] of density) {
  const base = `android/app/src/main/res/mipmap-${folder}`;
  await mkdir(base, { recursive: true });
  const icon = `web/icons/icon-${size}.png`;
  await copyFile(icon, `${base}/ic_launcher.png`);
  await copyFile(icon, `${base}/ic_launcher_round.png`);
  await copyFile('web/icons/icon-maskable-512.png', `${base}/ic_launcher_foreground.png`);
}
console.log(`Generated ${sizes.length + 1} web icons and Android launcher densities.`);
