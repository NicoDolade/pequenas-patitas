# Dockerfile — la misma pila de XAMPP (Apache + PHP), pero en un contenedor
FROM php:8.2-apache

# PDO para MySQL (cURL ya viene incluido en esta imagen)
RUN docker-php-ext-install pdo_mysql

# Apache escucha en el puerto que indique el proveedor (variable PORT); por defecto, 80
ENV PORT=80
RUN sed -i 's/80/${PORT}/g' /etc/apache2/ports.conf /etc/apache2/sites-available/000-default.conf

COPY . /var/www/html/

# Para que avisos.log (modo simulación) se pueda escribir sin restricciones de permisos
RUN chown -R www-data:www-data /var/www/html/api
