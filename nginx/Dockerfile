# Use the official Nginx image
FROM nginx:stable-alpine

# Remove the default nginx configuration file
RUN rm /etc/nginx/conf.d/default.conf

# Copy our custom nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose HTTP port only
EXPOSE 80
