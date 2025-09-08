import http from 'k6/http';
import {uuidv4} from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export let options = {
    vus: __ENV.VUS || 2,
    duration: __ENV.DURATION || '5m',
};

export default function () {
    const userId = uuidv4();
    const itemsCount = Math.floor(Math.random() * 5) + 1;

    const items = [];
    for (let i = 0; i < itemsCount; i++) {
        items.push({
            productId: uuidv4(),
            quantity: Math.floor(Math.random() * 10) + 1
        });
    }

    const payload = JSON.stringify({
        userId: userId,
        items: items
    });

    const headers = {
        'Content-Type': 'application/json',
        'accept': '*/*',
    };

    if (__ENV.SIMULATION_STRATEGY) {
        headers['X-simulation-strategy'] = __ENV.SIMULATION_STRATEGY;
    }

    const params = {
        headers: headers,
    };

    http.post(`http://${__ENV.TARGET_HOST}/orders`, payload, params);
}