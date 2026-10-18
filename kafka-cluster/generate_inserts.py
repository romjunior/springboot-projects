import random
import string

def generate_random_name():
    return ''.join(random.choices(string.ascii_letters, k=random.randint(3, 10)))

def generate_inserts(num_records=70000):
    with open('inserts.sql', 'w') as f:
        for i in range(num_records):
            name = generate_random_name()
            f.write(f"INSERT INTO ITEM (NAME) VALUES ('{name}');\n")

if __name__ == "__main__":
    generate_inserts()
    print("70.000 inserts gerados no arquivo 'inserts.sql'")