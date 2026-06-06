import { useEffect, useState } from "react";
import API_URL from "../api/api";

function TestBackend() {
  const [status, setStatus] = useState(null);

  useEffect(() => {
    fetch(`${API_URL}/public/status`)
      .then(res => res.json())
      .then(data => setStatus(data))
      .catch(err => console.error(err));
  }, []);

  return (
    <div className="container mt-4">
      <h3>Estado Backend</h3>
      <pre>{JSON.stringify(status, null, 2)}</pre>
    </div>
  );
}

export default TestBackend;
