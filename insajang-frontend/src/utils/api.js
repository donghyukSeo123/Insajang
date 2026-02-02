// src/utils/api.js
import axios from "axios";

/** @type {import('axios').AxiosInstance} */ //
const API = axios.create({
  baseURL: "http://localhost:8080", // 또는 process.env.REACT_APP_API_URL
  timeout: 5000,
});

export default API;