// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

export const environment = {
  production: false,
  API_URL_AUTH: `${document.location.hostname}:8082`,
  API_URL_PARTY: `https://${document.location.hostname}:8443/party`,
  API_URL_GAME_ROUND: `https://${document.location.hostname}:8443/round`,
  API_URL_GAME_WS: `wss://${document.location.hostname}:8443/round/ws`,
  API_URL_PLAYERS: `https://${document.location.hostname}:8444/player`,
  API_URL_RANKS: `https://${document.location.hostname}:8444/rank`
};
