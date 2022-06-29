import type { NextPage } from 'next'
import React, { useEffect, useState } from 'react';
import { Button, Table } from 'react-bootstrap';
import type { Category } from '../../modules/catalog/models/Category'
import { getCategories } from '../../modules/catalog/services/CategoryService'

const CategoryList: NextPage = () => {
  const [categories, setCategories] = useState<Category[]>([]);
  const [isLoading, setLoading] = useState(false);
  useEffect(() => {
    setLoading(true);
    getCategories()
      .then((data) => {
        setCategories(data);
        setLoading(false);
      });
  }, []);

  if (isLoading) return <p>Loading...</p>;
  if (!categories) return <p>No category</p>;

  return (
    <>
    <div className='row mt-5'>
      <div className='col-md-8'>
        <h2>Categories</h2>
      </div> 
      <div className='col-md-4 text-right'>
        <Button href="create-category">Create Category</Button> 
      </div>
    </div>
    <Table striped bordered hover>
      <thead>
        <tr>
          <th>#</th>
          <th>Name</th>
          <th>Description</th>
        </tr>
      </thead>
      <tbody>
        {categories.map((category) => (
          <tr key={category.id}>
            <td>{category.id}</td>
            <td>{category.name}</td>
            <td>{category.description}</td>
          </tr>
        ))}
        </tbody>
    </Table>
    </>
  )
}

export default CategoryList