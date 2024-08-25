import React from 'react';
import { useParams } from 'react-router-dom';

function CustomerPage() {
    // Estrai l'ID dall'URL
    const { id } = useParams();

    return (
        <div>
            <h1>Customer Page</h1>
            <p>ID del cliente: {id}</p>
        </div>
    );
}

export default CustomerPage;