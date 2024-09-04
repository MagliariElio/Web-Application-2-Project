import React from 'react';
import { useParams } from 'react-router-dom';

function ProfessionalPage() {
    // Estrai l'ID dall'URL
    const { id } = useParams();

    return (
        <div>
            <h1>Professional Page</h1>
            <p>ID del professional: {id}</p>
        </div>
    );
}

export default ProfessionalPage;